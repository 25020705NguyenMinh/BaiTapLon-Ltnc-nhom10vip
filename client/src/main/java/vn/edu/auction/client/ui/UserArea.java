package vn.edu.auction.client.ui;

import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import vn.edu.auction.client.api.UserSocketClient;
import vn.edu.auction.client.browse.BrowseController;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.enums.UserRole;

/** Ô user ở góc phải thanh trên: chưa đăng nhập => nút Đăng nhập; đã đăng nhập => chuông, chat, menu tài khoản. */
public final class UserArea {
    private UserArea() {}

    public static void render(HBox box, Runnable onChanged) {
        box.getChildren().clear();
        SessionData s = SessionData.getInstance();

        if (!s.isLoggedIn()) {
            UserSocketClient.getInstance().stop();
            ChatWindow.close();
            Button login = new Button("Đăng nhập");
            login.getStyleClass().add("outline-button");
            login.setOnAction(e -> BrowseController.openLogin((Stage) box.getScene().getWindow(), onChanged));
            box.getChildren().add(login);
            return;
        }

        UserSocketClient.getInstance().start(); // realtime thông báo + chat

        Button chat = new Button("💬");
        chat.getStyleClass().add("outline-button");
        chat.setOnAction(e -> ChatWindow.toggle(box.getScene().getWindow()));

        MenuButton me = new MenuButton("👤 " + s.getFullName());
        me.getStyleClass().add("outline-button");
        MenuItem wallet = new MenuItem("Ví");
        MenuItem profile = new MenuItem("Thông tin tài khoản");
        MenuItem logout = new MenuItem("Đăng xuất");
        wallet.setOnAction(e -> WalletDialog.show(box.getScene().getWindow()));
        profile.setOnAction(e -> ProfileDialog.show(box.getScene().getWindow(), onChanged));
        logout.setOnAction(e -> { ChatWindow.close(); s.clearSession(); onChanged.run(); });
        me.getItems().addAll(wallet, profile);
        if (s.getRole() == UserRole.ADMIN) {
            MenuItem admin = new MenuItem("Trang quản trị");
            admin.setOnAction(e -> AdminStage.show(box.getScene().getWindow()));
            me.getItems().add(admin);
        }
        me.getItems().addAll(new SeparatorMenuItem(), logout);

        box.getChildren().addAll(new BellButton(), chat, me);
    }
}
