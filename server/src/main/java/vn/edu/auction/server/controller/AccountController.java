package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.AccountService;
import vn.edu.auction.shared.dto.ExtraDto.*;

/** Hồ sơ cá nhân và ví của CHÍNH người đang đăng nhập (userId lấy từ JWT, không nhận từ client). */
@RestController
@RequestMapping("/api/me")
public class AccountController {
    private final AccountService service;

    public AccountController(AccountService service) { this.service = service; }

    @GetMapping("/profile")
    public ProfileResponse profile(Authentication a) { return service.profile(AuctionPrincipal.userId(a)); }

    @PutMapping("/profile")
    public ProfileResponse update(@RequestBody UpdateProfileRequest r, Authentication a) {
        return service.updateProfile(AuctionPrincipal.userId(a), r);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void password(@RequestBody ChangePasswordRequest r, Authentication a) {
        service.changePassword(AuctionPrincipal.userId(a), r);
    }

    @GetMapping("/wallet")
    public WalletResponse wallet(Authentication a) { return service.wallet(AuctionPrincipal.userId(a)); }

    @PostMapping("/wallet/deposit")
    public WalletResponse deposit(@RequestBody MoneyRequest r, Authentication a) {
        return service.deposit(AuctionPrincipal.userId(a), r);
    }

    @PostMapping("/wallet/withdraw")
    public WalletResponse withdraw(@RequestBody MoneyRequest r, Authentication a) {
        return service.withdraw(AuctionPrincipal.userId(a), r);
    }

    @GetMapping("/wallet/transactions")
    public List<WalletTxResponse> history(Authentication a) { return service.history(AuctionPrincipal.userId(a)); }
}
