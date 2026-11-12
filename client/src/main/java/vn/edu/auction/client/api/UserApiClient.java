package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import java.math.BigDecimal;
import java.util.List;
import vn.edu.auction.client.api.AuthApiClient.ApiException;
import vn.edu.auction.shared.dto.ExtraDto.*;

/** Ví, hồ sơ, thông báo, theo dõi phiên, follow người bán, chat. */
public class UserApiClient extends BaseApiClient {
    // ---- ví ----
    public WalletResponse wallet() throws ApiException {
        return call("GET", "/api/me/wallet", null, new TypeReference<WalletResponse>() {});
    }
    public WalletResponse deposit(BigDecimal v) throws ApiException {
        return call("POST", "/api/me/wallet/deposit", new MoneyRequest(v), new TypeReference<WalletResponse>() {});
    }
    public WalletResponse withdraw(BigDecimal v) throws ApiException {
        return call("POST", "/api/me/wallet/withdraw", new MoneyRequest(v), new TypeReference<WalletResponse>() {});
    }
    public List<WalletTxResponse> walletHistory() throws ApiException {
        return call("GET", "/api/me/wallet/transactions", null, new TypeReference<List<WalletTxResponse>>() {});
    }

    // ---- hồ sơ ----
    public ProfileResponse profile() throws ApiException {
        return call("GET", "/api/me/profile", null, new TypeReference<ProfileResponse>() {});
    }
    public ProfileResponse updateProfile(UpdateProfileRequest r) throws ApiException {
        return call("PUT", "/api/me/profile", r, new TypeReference<ProfileResponse>() {});
    }
    public void changePassword(ChangePasswordRequest r) throws ApiException {
        call("PUT", "/api/me/password", r, null);
    }

    // ---- thông báo ----
    public List<NotificationResponse> notifications() throws ApiException {
        return call("GET", "/api/notifications", null, new TypeReference<List<NotificationResponse>>() {});
    }
    public void readAllNotifications() throws ApiException {
        call("POST", "/api/notifications/read-all", null, null);
    }

    // ---- theo dõi phiên / tắt thông báo ----
    public WatchStateResponse getWatch(Long auctionId) throws ApiException {
        return call("GET", "/api/auctions/" + auctionId + "/watch", null, new TypeReference<WatchStateResponse>() {});
    }
    public WatchStateResponse setWatch(Long auctionId, boolean watching, boolean muted) throws ApiException {
        return call("PUT", "/api/auctions/" + auctionId + "/watch", new WatchRequest(watching, muted),
                new TypeReference<WatchStateResponse>() {});
    }

    // ---- follow người bán ----
    public FollowStateResponse getFollow(Long userId) throws ApiException {
        return call("GET", "/api/users/" + userId + "/follow", null, new TypeReference<FollowStateResponse>() {});
    }
    public void follow(Long userId) throws ApiException { call("POST", "/api/users/" + userId + "/follow", null, null); }
    public void unfollow(Long userId) throws ApiException { call("DELETE", "/api/users/" + userId + "/follow", null, null); }

    // ---- chat ----
    public List<UserBriefResponse> directory() throws ApiException {
        return call("GET", "/api/users/directory", null, new TypeReference<List<UserBriefResponse>>() {});
    }
    public List<ChatMessageResponse> chat(Long otherId) throws ApiException {
        return call("GET", "/api/chat/" + otherId, null, new TypeReference<List<ChatMessageResponse>>() {});
    }
    public ChatMessageResponse sendChat(Long otherId, String content) throws ApiException {
        return call("POST", "/api/chat/" + otherId, new SendChatRequest(content), new TypeReference<ChatMessageResponse>() {});
    }
}
