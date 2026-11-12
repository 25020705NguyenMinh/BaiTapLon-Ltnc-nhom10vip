package vn.edu.auction.server.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.AuctionEntity;
import vn.edu.auction.server.entity.ItemEntity;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.server.event.AuctionEventPublisher;
import vn.edu.auction.server.repository.AuctionRepository;
import vn.edu.auction.server.repository.BidTransactionRepository;
import vn.edu.auction.server.repository.ItemRepository;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.shared.dto.ExtraDto.AdminAuctionResponse;
import vn.edu.auction.shared.dto.ExtraDto.AdminUserResponse;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.shared.enums.ItemStatus;
import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

/** Các thao tác của quản trị viên (đã được SecurityConfig chặn bằng hasRole("ADMIN")). */
@Service
@Transactional
public class AdminService {
    private final UserRepository users;
    private final ItemRepository items;
    private final AuctionRepository auctions;
    private final BidTransactionRepository bids;
    private final AuctionEventPublisher publisher;
    private final SocialService social;

    public AdminService(UserRepository users, ItemRepository items, AuctionRepository auctions,
                        BidTransactionRepository bids, AuctionEventPublisher publisher, SocialService social) {
        this.users = users;
        this.items = items;
        this.auctions = auctions;
        this.bids = bids;
        this.publisher = publisher;
        this.social = social;
    }

    // ---------- Trang 1: tài khoản ----------
    @Transactional(readOnly = true)
    public List<AdminUserResponse> users() {
        return users.findAll().stream().filter(u -> u.getStatus() != UserStatus.BANNED)
                .map(u -> new AdminUserResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.getStatus()))
                .toList();
    }

    public void setUserStatus(Long adminId, Long id, UserStatus status) {
        AuctionRules.require(status == UserStatus.ACTIVE || status == UserStatus.INACTIVE,
                HttpStatus.BAD_REQUEST, "Chỉ được đặt ACTIVE hoặc INACTIVE.");
        target(adminId, id).setStatus(status);
    }

    /** "Xóa" = BANNED (xóa mềm) để không làm mồ côi bid / sản phẩm / lịch sử giao dịch của người đó. */
    public void deleteUser(Long adminId, Long id) { target(adminId, id).setStatus(UserStatus.BANNED); }

    private UserEntity target(Long adminId, Long id) {
        AuctionRules.require(!adminId.equals(id), HttpStatus.BAD_REQUEST, "Không thể thao tác trên chính tài khoản của bạn.");
        UserEntity u = users.findById(id).orElse(null);
        AuctionRules.require(u != null, HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản.");
        AuctionRules.require(u.getRole() != UserRole.ADMIN, HttpStatus.FORBIDDEN, "Không thể thao tác trên tài khoản quản trị.");
        return u;
    }

    // ---------- Trang 2: sản phẩm chờ duyệt (DRAFT) ----------
    @Transactional(readOnly = true)
    public List<ItemResponse> pendingItems() {
        return items.findByStatus(ItemStatus.DRAFT).stream()
                .map(i -> new ItemResponse(i.getId(), i.getSellerId(), i.getName(), i.getDescription(),
                        i.getStartingPrice(), i.getStatus())).toList();
    }

    public void approveItem(Long id) { review(id, ItemStatus.ACTIVE, "đã được duyệt, bạn có thể tạo phiên đấu giá."); }
    public void rejectItem(Long id) { review(id, ItemStatus.REMOVED, "đã bị từ chối."); }

    private void review(Long id, ItemStatus next, String text) {
        ItemEntity item = items.findByIdForUpdate(id).orElse(null);
        AuctionRules.require(item != null, HttpStatus.NOT_FOUND, "Không tìm thấy sản phẩm.");
        AuctionRules.require(item.getStatus() == ItemStatus.DRAFT, HttpStatus.CONFLICT,
                "Sản phẩm không ở trạng thái chờ duyệt.");
        item.setStatus(next);
        social.notify(item.getSellerId(), "ITEM_REVIEW", "Sản phẩm \"" + item.getName() + "\" " + text, item.getId());
    }

    // ---------- Trang 3: phiên đấu giá ----------
    @Transactional(readOnly = true)
    public List<AdminAuctionResponse> auctions() {
        return auctions.findAll().stream().map(a -> new AdminAuctionResponse(a.getId(),
                items.findById(a.getItemId()).map(ItemEntity::getName).orElse("?"), a.getCurrentPrice(),
                a.getHighestBidderId(), a.getEndTime(), a.getStatus(), a.isHidden(),
                bids.countByAuctionId(a.getId()))).toList();
    }

    /** Dừng = CANCELED. BidService đã từ chối bid vào phiên không còn mở. Phát AUCTION_FINISHED (status CANCELED). */
    public void stopAuction(Long id) {
        AuctionEntity a = auctions.findByIdForUpdate(id).orElse(null);
        AuctionRules.require(a != null, HttpStatus.NOT_FOUND, "Không tìm thấy phiên đấu giá.");
        AuctionRules.require(a.getStatus() == AuctionStatus.OPEN || a.getStatus() == AuctionStatus.RUNNING,
                HttpStatus.CONFLICT, "Phiên không còn hoạt động.");
        a.setStatus(AuctionStatus.CANCELED);
        auctions.save(a);
        publisher.publishAuctionFinished(a);
        social.onAuctionEnded(a, "đã bị quản trị viên dừng.");
    }

    public void setHidden(Long id, boolean hidden) {
        AuctionEntity a = auctions.findByIdForUpdate(id).orElse(null);
        AuctionRules.require(a != null, HttpStatus.NOT_FOUND, "Không tìm thấy phiên đấu giá.");
        a.setHidden(hidden);
    }
}
