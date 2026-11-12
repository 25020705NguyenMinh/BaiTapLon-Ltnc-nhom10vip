package vn.edu.auction.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.UserApiClient;
import vn.edu.auction.client.api.UserSocketClient;
import vn.edu.auction.shared.dto.ExtraDto.NotificationResponse;

/** Chuông thông báo: số chưa đọc + danh sách thả xuống, nhận thông báo mới theo thời gian thực. */
public final class BellButton extends StackPane {
    private final UserApiClient api = new UserApiClient();
    private final ObservableList<NotificationResponse> data = FXCollections.observableArrayList();
    private final Label badge = new Label();
    private final Popup popup = new Popup();
    private Runnable unsub;

    public BellButton() {
        Button bell = new Button("🔔");
        bell.getStyleClass().add("outline-button");
        badge.setStyle("-fx-background-color:#e53935;-fx-text-fill:white;-fx-background-radius:9;"
                + "-fx-padding:0 5;-fx-font-size:10;");
        badge.setMouseTransparent(true);
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        getChildren().addAll(bell, badge);

        ListView<NotificationResponse> list = new ListView<>(data);
        list.setPrefSize(340, 360);
        list.setPlaceholder(new Label("Chưa có thông báo."));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(NotificationResponse n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) { setText(null); setStyle(""); return; }
                setText(n.message() + "\n" + UiKit.dt(n.createdAt()));
                setWrapText(true);
                setPrefWidth(320);
                setStyle(n.seen() ? "" : "-fx-font-weight:bold;");
            }
        });
        VBox box = new VBox(list);
        box.setStyle("-fx-background-color:white;-fx-border-color:#cccccc;");
        popup.getContent().add(box);
        popup.setAutoHide(true);
        popup.setOnHidden(e -> markAllSeen());

        bell.setOnAction(e -> {
            if (popup.isShowing()) { popup.hide(); return; }
            Bounds b = bell.localToScreen(bell.getBoundsInLocal());
            popup.show(bell, b.getMaxX() - 340, b.getMaxY() + 4);
        });

        reload();
        unsub = UserSocketClient.getInstance().subscribe(this::onSocket);
        sceneProperty().addListener((o, old, sc) -> {
            if (sc == null && unsub != null) { unsub.run(); unsub = null; }
        });
    }

    private void reload() {
        FxAsync.run(api::notifications, l -> { data.setAll(l); updateBadge(); }, e -> { });
    }

    private void onSocket(JsonNode msg) {
        if (!"NOTIFICATION".equals(msg.path("kind").asText())) return;
        try {
            data.add(0, BaseApiClient.MAPPER.treeToValue(msg.get("payload"), NotificationResponse.class));
            updateBadge();
        } catch (Exception ignored) {
            // gói tin hỏng thì bỏ qua
        }
    }

    private void markAllSeen() {
        if (data.stream().allMatch(NotificationResponse::seen)) return;
        FxAsync.run(() -> { api.readAllNotifications(); return Boolean.TRUE; }, ok -> {
            data.replaceAll(n -> new NotificationResponse(n.id(), n.type(), n.message(), n.refId(), true, n.createdAt()));
            updateBadge();
        }, e -> { });
    }

    private void updateBadge() {
        long unread = data.stream().filter(n -> !n.seen()).count();
        badge.setText(unread > 99 ? "99+" : String.valueOf(unread));
        badge.setVisible(unread > 0);
    }
}
