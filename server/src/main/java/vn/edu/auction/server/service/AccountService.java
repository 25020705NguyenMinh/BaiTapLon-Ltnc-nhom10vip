package vn.edu.auction.server.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.server.entity.WalletTransactionEntity;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.server.repository.WalletTransactionRepository;
import vn.edu.auction.shared.dto.ExtraDto.*;

/** Hồ sơ cá nhân, đổi mật khẩu và ví (nạp / rút / lịch sử). */
@Service
@Transactional
public class AccountService {
    private final UserRepository users;
    private final WalletTransactionRepository txs;
    private final PasswordEncoder encoder;
    private final Clock clock;

    public AccountService(UserRepository users, WalletTransactionRepository txs,
                          PasswordEncoder encoder, Clock auctionClock) {
        this.users = users;
        this.txs = txs;
        this.encoder = encoder;
        this.clock = auctionClock;
    }

    private UserEntity lockedUser(Long id) {
        UserEntity u = users.findByIdForUpdate(id).orElse(null);
        AuctionRules.require(u != null, HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        return u;
    }

    // ---------- hồ sơ ----------
    @Transactional(readOnly = true)
    public ProfileResponse profile(Long id) {
        UserEntity u = users.findById(id).orElse(null);
        AuctionRules.require(u != null, HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        return toProfile(u);
    }

    public ProfileResponse updateProfile(Long id, UpdateProfileRequest r) {
        AuctionRules.require(r != null && r.fullName() != null && !r.fullName().isBlank() && r.fullName().length() <= 100,
                HttpStatus.BAD_REQUEST, "Họ tên không hợp lệ.");
        String phone = r.phone() == null ? "" : r.phone().trim();
        AuctionRules.require(phone.isEmpty() || phone.matches("\\d{9,11}"),
                HttpStatus.BAD_REQUEST, "Số điện thoại phải gồm 9–11 chữ số.");
        String address = r.address() == null ? "" : r.address().trim();
        AuctionRules.require(address.length() <= 255, HttpStatus.BAD_REQUEST, "Địa chỉ quá dài.");
        UserEntity u = lockedUser(id);
        u.setFullName(r.fullName().trim());
        u.setPhone(phone.isEmpty() ? null : phone);
        u.setAddress(address.isEmpty() ? null : address);
        return toProfile(u);
    }

    public void changePassword(Long id, ChangePasswordRequest r) {
        AuctionRules.require(r != null && r.oldPassword() != null && r.newPassword() != null,
                HttpStatus.BAD_REQUEST, "Thiếu mật khẩu.");
        AuctionRules.require(r.newPassword().length() >= 8,
                HttpStatus.BAD_REQUEST, "Mật khẩu mới phải có ít nhất 8 ký tự.");
        UserEntity u = lockedUser(id);
        AuctionRules.require(encoder.matches(r.oldPassword(), u.getPasswordHash()),
                HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng.");
        u.setPasswordHash(encoder.encode(r.newPassword()));
    }

    private ProfileResponse toProfile(UserEntity u) {
        return new ProfileResponse(u.getId(), u.getFullName(), u.getEmail(), u.getPhone(), u.getAddress(), u.getRole());
    }

    // ---------- ví ----------
    @Transactional(readOnly = true)
    public WalletResponse wallet(Long id) {
        UserEntity u = users.findById(id).orElse(null);
        AuctionRules.require(u != null, HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        return new WalletResponse(u.getWalletBalance());
    }

    public WalletResponse deposit(Long id, MoneyRequest r) { return apply(id, r, true); }
    public WalletResponse withdraw(Long id, MoneyRequest r) { return apply(id, r, false); }

    private WalletResponse apply(Long id, MoneyRequest r, boolean deposit) {
        BigDecimal amount = AuctionRules.money(r == null ? null : r.amount());
        UserEntity u = lockedUser(id); // khóa dòng user: 2 giao dịch ví đồng thời không làm sai số dư
        BigDecimal balance = u.getWalletBalance();
        AuctionRules.require(deposit || balance.compareTo(amount) >= 0, HttpStatus.CONFLICT, "Số dư không đủ.");
        BigDecimal after = deposit ? balance.add(amount) : balance.subtract(amount);
        u.setWalletBalance(after);
        WalletTransactionEntity t = new WalletTransactionEntity();
        t.userId = id;
        t.type = deposit ? "DEPOSIT" : "WITHDRAW";
        t.amount = amount;
        t.balanceAfter = after;
        t.createdAt = LocalDateTime.now(clock);
        txs.save(t);
        return new WalletResponse(after);
    }

    @Transactional(readOnly = true)
    public List<WalletTxResponse> history(Long id) {
        return txs.findTop100ByUserIdOrderByIdDesc(id).stream()
                .map(t -> new WalletTxResponse(t.id, t.type, t.amount, t.balanceAfter, t.createdAt)).toList();
    }
}
