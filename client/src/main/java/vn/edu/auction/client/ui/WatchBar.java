package vn.edu.auction.client.ui;

import java.util.List;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.ItemApiClient;
import vn.edu.auction.client.api.UserApiClient;
import vn.edu.auction.client.model.SessionData;

/** Theo dõi phiên, tắt/bật thông báo của phiên, và theo dõi người bán. Nhúng vào dialog chi tiết phiên. */
public final class WatchBar extends HBox {
    private final UserApiClient api = new UserApiClient();
    private final Button watchBtn = new Button();
    private final Button muteBtn = new Button();
    private final Button followBtn = new Button();
    private final Long auctionId;
    private boolean watching;
    private boolean muted;
    private boolean following;
    private Long sellerId;

    public WatchBar(Long auctionId, Long itemId) {
        super(8);
        this.auctionId = auctionId;
        for (Button b : List.of(watchBtn, muteBtn, followBtn)) b.getStyleClass().add("outline-button");
        getChildren().addAll(watchBtn, muteBtn, followBtn);
        followBtn.setVisible(false);
        followBtn.setManaged(false);
        refreshText();

        if (!SessionData.getInstance().isLoggedIn()) {
            watchBtn.setDisable(true);
            muteBtn.setDisable(true);
            watchBtn.setTooltip(new Tooltip("Đăng nhập để theo dõi phiên này"));
            return;
        }
        watchBtn.setOnAction(e -> pushWatch(!watching, muted));
        muteBtn.setOnAction(e -> pushWatch(true, !muted));
        followBtn.setOnAction(e -> toggleFollow());

        FxAsync.run(() -> api.getWatch(auctionId), s -> { watching = s.watching(); muted = s.muted(); refreshText(); }, e -> { });
        FxAsync.run(() -> new ItemApiClient().getItemById(itemId).getSellerId(), sid -> {
            sellerId = sid;
            if (sid != null && !sid.equals(SessionData.getInstance().getUserId())) {
                followBtn.setVisible(true);
                followBtn.setManaged(true);
                FxAsync.run(() -> api.getFollow(sid), f -> { following = f.following(); refreshText(); }, e -> { });
            }
        }, e -> { });
    }

    private void pushWatch(boolean w, boolean m) {
        FxAsync.run(() -> api.setWatch(auctionId, w, m),
                s -> { watching = s.watching(); muted = s.muted(); refreshText(); }, this::error);
    }

    private void toggleFollow() {
        boolean target = !following;
        FxAsync.run(() -> {
            if (target) api.follow(sellerId); else api.unfollow(sellerId);
            return target;
        }, v -> { following = v; refreshText(); }, this::error);
    }

    private void error(Throwable t) {
        new Alert(Alert.AlertType.WARNING, BaseApiClient.describe(t)).show();
    }

    private void refreshText() {
        watchBtn.setText(watching ? "★ Đang theo dõi" : "☆ Theo dõi sản phẩm");
        muteBtn.setText(muted ? "🔔 Bật thông báo" : "🔕 Tắt thông báo");
        muteBtn.setDisable(!watching);
        followBtn.setText(following ? "✓ Đang theo dõi người bán" : "＋ Theo dõi người bán");
    }
}
