package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.SocialService;
import vn.edu.auction.shared.dto.ExtraDto.*;

/** Thông báo, theo dõi phiên, follow người bán, chat. */
@RestController
public class SocialController {
    private final SocialService social;

    public SocialController(SocialService social) { this.social = social; }

    // ----- thông báo -----
    @GetMapping("/api/notifications")
    public List<NotificationResponse> notifications(Authentication a) {
        return social.myNotifications(AuctionPrincipal.userId(a));
    }

    @PostMapping("/api/notifications/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void readAll(Authentication a) { social.markAllSeen(AuctionPrincipal.userId(a)); }

    // ----- theo dõi phiên / tắt thông báo -----
    @GetMapping("/api/auctions/{id}/watch")
    public WatchStateResponse getWatch(@PathVariable("id") Long id, Authentication a) {
        return social.getWatch(AuctionPrincipal.userId(a), id);
    }

    @PutMapping("/api/auctions/{id}/watch")
    public WatchStateResponse setWatch(@PathVariable("id") Long id, @RequestBody WatchRequest r, Authentication a) {
        return social.setWatch(AuctionPrincipal.userId(a), id, r);
    }

    // ----- follow người bán -----
    @GetMapping("/api/users/{id}/follow")
    public FollowStateResponse getFollow(@PathVariable("id") Long id, Authentication a) {
        return social.getFollow(AuctionPrincipal.userId(a), id);
    }

    @PostMapping("/api/users/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void follow(@PathVariable("id") Long id, Authentication a) { social.follow(AuctionPrincipal.userId(a), id); }

    @DeleteMapping("/api/users/{id}/follow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfollow(@PathVariable("id") Long id, Authentication a) { social.unfollow(AuctionPrincipal.userId(a), id); }

    // ----- chat -----
    @GetMapping("/api/users/directory")
    public List<UserBriefResponse> directory(Authentication a) { return social.directory(AuctionPrincipal.userId(a)); }

    @GetMapping("/api/chat/{userId}")
    public List<ChatMessageResponse> chat(@PathVariable("userId") Long other, Authentication a) {
        return social.conversation(AuctionPrincipal.userId(a), other);
    }

    @PostMapping("/api/chat/{userId}")
    public ChatMessageResponse send(@PathVariable("userId") Long to, @RequestBody SendChatRequest r, Authentication a) {
        return social.sendChat(AuctionPrincipal.userId(a), to, r);
    }
}
