package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.AdminService;
import vn.edu.auction.shared.dto.ExtraDto.*;
import vn.edu.auction.shared.dto.ItemResponse;

/** /api/admin/** chỉ cho ROLE_ADMIN (cấu hình trong SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;

    public AdminController(AdminService admin) { this.admin = admin; }

    @GetMapping("/users")
    public List<AdminUserResponse> users() { return admin.users(); }

    @PatchMapping("/users/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@PathVariable("id") Long id, @RequestBody StatusRequest r, Authentication a) {
        admin.setUserStatus(AuctionPrincipal.userId(a), id, r.status());
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id, Authentication a) {
        admin.deleteUser(AuctionPrincipal.userId(a), id);
    }

    @GetMapping("/items/pending")
    public List<ItemResponse> pending() { return admin.pendingItems(); }

    @PostMapping("/items/{id}/approve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void approve(@PathVariable("id") Long id) { admin.approveItem(id); }

    @PostMapping("/items/{id}/reject")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reject(@PathVariable("id") Long id) { admin.rejectItem(id); }

    @GetMapping("/auctions")
    public List<AdminAuctionResponse> auctions() { return admin.auctions(); }

    @PostMapping("/auctions/{id}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop(@PathVariable("id") Long id) { admin.stopAuction(id); }

    @PutMapping("/auctions/{id}/hidden")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hidden(@PathVariable("id") Long id, @RequestParam("value") boolean value) {
        admin.setHidden(id, value);
    }
}
