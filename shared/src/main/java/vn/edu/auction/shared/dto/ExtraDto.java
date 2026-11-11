package vn.edu.auction.shared.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

/** Các DTO bổ sung: ví, tài khoản, admin, thông báo, theo dõi, chat. */
public final class ExtraDto {
    private ExtraDto() {}

    // Ví
    public record MoneyRequest(BigDecimal amount) {}
    public record WalletResponse(BigDecimal balance) {}
    public record WalletTxResponse(Long id, String type, BigDecimal amount, BigDecimal balanceAfter, LocalDateTime createdAt) {}
    // Tài khoản
    public record ProfileResponse(Long id, String fullName, String email, String phone, String address, UserRole role) {}
    public record UpdateProfileRequest(String fullName, String phone, String address) {}
    public record ChangePasswordRequest(String oldPassword, String newPassword) {}
    // Admin
    public record AdminUserResponse(Long id, String fullName, String email, UserRole role, UserStatus status) {}
    public record StatusRequest(UserStatus status) {}
    public record AdminAuctionResponse(Long id, String itemName, BigDecimal currentPrice, Long highestBidderId,
                                       LocalDateTime endTime, AuctionStatus status, boolean hidden, long bidCount) {}
    // Thông báo / theo dõi / chat
    public record NotificationResponse(Long id, String type, String message, Long refId, boolean seen, LocalDateTime createdAt) {}
    public record WatchRequest(boolean watching, boolean muted) {}
    public record WatchStateResponse(boolean watching, boolean muted) {}
    public record FollowStateResponse(boolean following) {}
    public record UserBriefResponse(Long id, String fullName, UserRole role) {}
    public record SendChatRequest(String content) {}
    public record ChatMessageResponse(Long id, Long senderId, Long receiverId, String content, LocalDateTime sentAt) {}
}
