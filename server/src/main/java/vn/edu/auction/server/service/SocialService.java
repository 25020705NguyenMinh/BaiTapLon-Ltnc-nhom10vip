package vn.edu.auction.server.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.*;
import vn.edu.auction.server.event.BidAcceptedEvent;
import vn.edu.auction.server.event.UserSocketHandler;
import vn.edu.auction.server.repository.*;
import vn.edu.auction.shared.dto.ExtraDto.*;
import vn.edu.auction.shared.enums.UserStatus;

/** Thông báo, theo dõi phiên (có tắt thông báo), follow người bán, chat. */
@Service
@Transactional
public class SocialService {
    private final NotificationRepository notifications;
    private final AuctionWatchRepository watches;
    private final UserFollowRepository follows;
    private final ChatMessageRepository chats;
    private final AuctionRepository auctions;
    private final ItemRepository items;
    private final UserRepository users;
    private final UserSocketHandler socket;
    private final Clock clock;

    public SocialService(NotificationRepository notifications, AuctionWatchRepository watches,
                         UserFollowRepository follows, ChatMessageRepository chats, AuctionRepository auctions,
                         ItemRepository items, UserRepository users, UserSocketHandler socket, Clock auctionClock) {
        this.notifications = notifications;
        this.watches = watches;
        this.follows = follows;
        this.chats = chats;
        this.auctions = auctions;
        this.items = items;
        this.users = users;
        this.socket = socket;
        this.clock = auctionClock;
    }

    // ================= THÔNG BÁO =================
    public void notify(Long userId, String type, String message, Long refId) {
        NotificationEntity n = new NotificationEntity();
        n.userId = userId;
        n.type = type;
        n.message = message;
        n.refId = refId;
        n.seen = false;
        n.createdAt = LocalDateTime.now(clock);
        notifications.save(n);
        socket.pushAfterCommit(userId, "NOTIFICATION", toDto(n));
    }

    /** Chạy đồng bộ trong transaction đặt giá: thông báo lưu cùng bid, bid rollback thì thông báo cũng mất. */
    @EventListener
    public void onBidAccepted(BidAcceptedEvent e) {
        AuctionEntity a = auctions.findById(e.auctionId()).orElse(null);
        ItemEntity item = a == null ? null : items.findById(a.getItemId()).orElse(null);
        if (item == null) return;
        Long bidder = e.highestBidderId();
        // Người đã đặt giá tự động theo dõi phiên
        if (watches.findByUserIdAndAuctionId(bidder, a.getId()).isEmpty()) {
            AuctionWatchEntity w = new AuctionWatchEntity();
            w.userId = bidder;
            w.auctionId = a.getId();
            w.muted = false;
            watches.save(w);
        }
        Set<Long> targets = new HashSet<>();
        watches.findByAuctionIdAndMutedFalse(a.getId()).forEach(w -> targets.add(w.userId));
        boolean sellerMuted = watches.findByUserIdAndAuctionId(item.getSellerId(), a.getId())
                .map(w -> w.muted).orElse(false);
        if (!sellerMuted) targets.add(item.getSellerId());
        targets.remove(bidder);
        String msg = "Có giá mới " + e.currentPrice().toPlainString() + " đ cho \"" + item.getName() + "\"";
        targets.forEach(uid -> notify(uid, "NEW_BID", msg, a.getId()));
    }

    /** Seller đưa sản phẩm mới lên đấu giá => báo cho những người đang follow seller. */
    public void onNewAuction(AuctionEntity a, ItemEntity item) {
        String seller = users.findById(item.getSellerId()).map(UserEntity::getFullName).orElse("Người bán");
        follows.findBySellerId(item.getSellerId()).forEach(f -> notify(f.followerId, "NEW_ITEM",
                seller + " vừa đưa lên đấu giá: \"" + item.getName() + "\"", a.getId()));
    }

    /** Phiên kết thúc hoặc bị admin dừng. */
    public void onAuctionEnded(AuctionEntity a, String text) {
        ItemEntity item = items.findById(a.getItemId()).orElse(null);
        if (item == null) return;
        Set<Long> targets = new HashSet<>();
        watches.findByAuctionIdAndMutedFalse(a.getId()).forEach(w -> targets.add(w.userId));
        targets.add(item.getSellerId());
        targets.forEach(uid -> notify(uid, "AUCTION_ENDED", "Phiên \"" + item.getName() + "\" " + text, a.getId()));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> myNotifications(Long uid) {
        return notifications.findTop50ByUserIdOrderByIdDesc(uid).stream().map(this::toDto).toList();
    }

    public void markAllSeen(Long uid) { notifications.markAllSeen(uid); }

    private NotificationResponse toDto(NotificationEntity n) {
        return new NotificationResponse(n.id, n.type, n.message, n.refId, n.seen, n.createdAt);
    }

    // ================= THEO DÕI PHIÊN / TẮT THÔNG BÁO =================
    @Transactional(readOnly = true)
    public WatchStateResponse getWatch(Long uid, Long auctionId) {
        return watches.findByUserIdAndAuctionId(uid, auctionId)
                .map(w -> new WatchStateResponse(true, w.muted)).orElse(new WatchStateResponse(false, false));
    }

    public WatchStateResponse setWatch(Long uid, Long auctionId, WatchRequest r) {
        AuctionRules.require(auctions.existsById(auctionId), HttpStatus.NOT_FOUND, "Không tìm thấy phiên đấu giá.");
        var existing = watches.findByUserIdAndAuctionId(uid, auctionId);
        if (r == null || !r.watching()) {
            existing.ifPresent(watches::delete);
            return new WatchStateResponse(false, false);
        }
        AuctionWatchEntity w = existing.orElseGet(AuctionWatchEntity::new);
        w.userId = uid;
        w.auctionId = auctionId;
        w.muted = r.muted();
        watches.save(w);
        return new WatchStateResponse(true, w.muted);
    }

    // ================= FOLLOW NGƯỜI BÁN =================
    @Transactional(readOnly = true)
    public FollowStateResponse getFollow(Long uid, Long sellerId) {
        return new FollowStateResponse(follows.findByFollowerIdAndSellerId(uid, sellerId).isPresent());
    }

    public void follow(Long uid, Long sellerId) {
        AuctionRules.require(!uid.equals(sellerId), HttpStatus.BAD_REQUEST, "Không thể tự theo dõi chính mình.");
        AuctionRules.require(users.existsById(sellerId), HttpStatus.NOT_FOUND, "Không tìm thấy người dùng.");
        if (follows.findByFollowerIdAndSellerId(uid, sellerId).isEmpty()) {
            UserFollowEntity f = new UserFollowEntity();
            f.followerId = uid;
            f.sellerId = sellerId;
            follows.save(f);
        }
    }

    public void unfollow(Long uid, Long sellerId) {
        follows.findByFollowerIdAndSellerId(uid, sellerId).ifPresent(follows::delete);
    }

    // ================= CHAT =================
    @Transactional(readOnly = true)
    public List<UserBriefResponse> directory(Long uid) {
        return users.findAll().stream()
                .filter(u -> u.getStatus() == UserStatus.ACTIVE && !u.getId().equals(uid))
                .map(u -> new UserBriefResponse(u.getId(), u.getFullName(), u.getRole())).toList();
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> conversation(Long me, Long other) {
        return chats.conversation(me, other).stream().map(this::toDto).toList();
    }

    public ChatMessageResponse sendChat(Long me, Long to, SendChatRequest r) {
        AuctionRules.require(!me.equals(to), HttpStatus.BAD_REQUEST, "Không thể nhắn cho chính mình.");
        AuctionRules.require(r != null && r.content() != null && !r.content().isBlank() && r.content().length() <= 1000,
                HttpStatus.BAD_REQUEST, "Nội dung tin nhắn không hợp lệ (1–1000 ký tự).");
        UserEntity receiver = users.findById(to).orElse(null);
        AuctionRules.require(receiver != null && receiver.getStatus() == UserStatus.ACTIVE,
                HttpStatus.NOT_FOUND, "Người nhận không tồn tại hoặc không hoạt động.");
        ChatMessageEntity m = new ChatMessageEntity();
        m.senderId = me;
        m.receiverId = to;
        m.content = r.content().trim();
        m.sentAt = LocalDateTime.now(clock);
        chats.save(m);
        ChatMessageResponse dto = toDto(m);
        socket.pushAfterCommit(to, "CHAT", dto);
        return dto;
    }

    private ChatMessageResponse toDto(ChatMessageEntity m) {
        return new ChatMessageResponse(m.id, m.senderId, m.receiverId, m.content, m.sentAt);
    }
}
