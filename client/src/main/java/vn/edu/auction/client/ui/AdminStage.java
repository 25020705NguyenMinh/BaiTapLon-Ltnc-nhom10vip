package vn.edu.auction.client.ui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import vn.edu.auction.client.api.AdminApiClient;
import vn.edu.auction.client.api.AuctionRealtimeClient;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.shared.dto.ExtraDto.AdminAuctionResponse;
import vn.edu.auction.shared.dto.ExtraDto.AdminUserResponse;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.UserStatus;

/** Trang quản trị riêng cho ADMIN: (1) tài khoản, (2) sản phẩm chờ duyệt, (3) phiên đấu giá. */
public final class AdminStage {
    private static Stage stage;

    private AdminStage() {}

    private interface ItemAction { void run(ItemResponse i) throws Exception; }

    public static void show(Window owner) {
        if (stage != null && stage.isShowing()) { stage.toFront(); return; }
        AdminApiClient api = new AdminApiClient();
        Label msg = new Label();

        // ---------- Trang 1: tài khoản ----------
        TableView<AdminUserResponse> users = table();
        UiKit.col(users, "ID", AdminUserResponse::id);
        UiKit.col(users, "Họ tên", AdminUserResponse::fullName);
        UiKit.col(users, "Email", AdminUserResponse::email);
        UiKit.col(users, "Vai trò", AdminUserResponse::role);
        UiKit.col(users, "Trạng thái", AdminUserResponse::status);
        Runnable loadUsers = () -> FxAsync.run(api::users, l -> users.getItems().setAll(l),
                e -> msg.setText(BaseApiClient.describe(e)));
        Button toggle = new Button("Vô hiệu hóa / Kích hoạt");
        toggle.setOnAction(e -> {
            AdminUserResponse u = users.getSelectionModel().getSelectedItem();
            if (u == null) { msg.setText("Hãy chọn một tài khoản."); return; }
            UserStatus next = u.status() == UserStatus.ACTIVE ? UserStatus.INACTIVE : UserStatus.ACTIVE;
            UiKit.act(msg, () -> { api.setUserStatus(u.id(), next); return Boolean.TRUE; }, loadUsers);
        });
        Button delete = new Button("Xóa tài khoản");
        delete.setOnAction(e -> {
            AdminUserResponse u = users.getSelectionModel().getSelectedItem();
            if (u == null) { msg.setText("Hãy chọn một tài khoản."); return; }
            Alert a = new Alert(Alert.AlertType.CONFIRMATION, "Xóa tài khoản " + u.email() + "?", ButtonType.YES, ButtonType.NO);
            a.showAndWait().filter(b -> b == ButtonType.YES)
                    .ifPresent(b -> UiKit.act(msg, () -> { api.deleteUser(u.id()); return Boolean.TRUE; }, loadUsers));
        });

        // ---------- Trang 2: sản phẩm chờ phê duyệt ----------
        TableView<ItemResponse> pending = table();
        UiKit.col(pending, "ID", ItemResponse::getId);
        UiKit.col(pending, "Sản phẩm", ItemResponse::getName);
        UiKit.col(pending, "Mô tả", ItemResponse::getDescription);
        UiKit.col(pending, "Giá khởi điểm", i -> UiKit.money(i.getStartingPrice()));
        UiKit.col(pending, "Người bán #", ItemResponse::getSellerId);
        Runnable loadPending = () -> FxAsync.run(api::pendingItems, l -> pending.getItems().setAll(l),
                e -> msg.setText(BaseApiClient.describe(e)));
        Button approve = new Button("Duyệt");
        Button reject = new Button("Từ chối");
        approve.setOnAction(e -> review(pending, msg, i -> api.approveItem(i.getId()), loadPending));
        reject.setOnAction(e -> review(pending, msg, i -> api.rejectItem(i.getId()), loadPending));

        // ---------- Trang 3: phiên đang đấu giá ----------
        TableView<AdminAuctionResponse> auctions = table();
        UiKit.col(auctions, "ID", AdminAuctionResponse::id);
        UiKit.col(auctions, "Sản phẩm", AdminAuctionResponse::itemName);
        UiKit.col(auctions, "Giá hiện tại", a -> UiKit.money(a.currentPrice()));
        UiKit.col(auctions, "Người dẫn đầu #", a -> a.highestBidderId() == null ? "—" : a.highestBidderId());
        UiKit.col(auctions, "Số lượt bid", AdminAuctionResponse::bidCount);
        UiKit.col(auctions, "Kết thúc", a -> UiKit.dt(a.endTime()));
        UiKit.col(auctions, "Trạng thái", AdminAuctionResponse::status);
        UiKit.col(auctions, "Hiển thị", a -> a.hidden() ? "Đang ẩn" : "Hiển thị");
        Runnable loadAuctions = () -> FxAsync.run(api::auctions, l -> auctions.getItems().setAll(l),
                e -> msg.setText(BaseApiClient.describe(e)));
        Button stop = new Button("Dừng đấu giá");
        Button hide = new Button("Ẩn / Hiện phiên");
        stop.setOnAction(e -> {
            AdminAuctionResponse a = auctions.getSelectionModel().getSelectedItem();
            if (a == null) { msg.setText("Hãy chọn một phiên."); return; }
            UiKit.act(msg, () -> { api.stopAuction(a.id()); return Boolean.TRUE; }, loadAuctions);
        });
        hide.setOnAction(e -> {
            AdminAuctionResponse a = auctions.getSelectionModel().getSelectedItem();
            if (a == null) { msg.setText("Hãy chọn một phiên."); return; }
            UiKit.act(msg, () -> { api.setHidden(a.id(), !a.hidden()); return Boolean.TRUE; }, loadAuctions);
        });

        TabPane tabs = new TabPane(
                tab("Tài khoản", users, loadUsers, toggle, delete),
                tab("Sản phẩm chờ duyệt", pending, loadPending, approve, reject),
                tab("Phiên đấu giá", auctions, loadAuctions, stop, hide));
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox root = new VBox(6, tabs, msg);
        VBox.setVgrow(tabs, Priority.ALWAYS);
        root.setPadding(new Insets(8));

        stage = new Stage();
        stage.initOwner(owner);
        stage.setTitle("Trang quản trị");
        Scene scene = new Scene(root, 960, 560);
        UiTheme.apply(scene);
        stage.setScene(scene);

        loadUsers.run();
        loadPending.run();
        loadAuctions.run();
        // Theo dõi realtime: có bid mới / phiên kết thúc thì trang 3 tự làm mới
        Runnable unsub = AuctionRealtimeClient.getInstance().subscribe(ev -> loadAuctions.run());
        stage.setOnHidden(e -> unsub.run());
        stage.show();
    }

    private static <T> TableView<T> table() {
        TableView<T> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        t.setPlaceholder(new Label("Không có dữ liệu."));
        return t;
    }

    private static <T> Tab tab(String title, TableView<T> table, Runnable reload, Button... actions) {
        Button refresh = new Button("Làm mới");
        refresh.setOnAction(e -> reload.run());
        HBox bar = new HBox(8, actions);
        bar.getChildren().add(refresh);
        VBox box = new VBox(8, table, bar);
        VBox.setVgrow(table, Priority.ALWAYS);
        box.setPadding(new Insets(8));
        return new Tab(title, box);
    }

    private static void review(TableView<ItemResponse> t, Label msg, ItemAction action, Runnable reload) {
        ItemResponse i = t.getSelectionModel().getSelectedItem();
        if (i == null) { msg.setText("Hãy chọn một sản phẩm."); return; }
        UiKit.act(msg, () -> { action.run(i); return Boolean.TRUE; }, reload);
    }
}
