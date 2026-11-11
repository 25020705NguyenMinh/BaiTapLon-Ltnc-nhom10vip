package vn.edu.auction.client.browse;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import vn.edu.auction.client.I18n;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.client.controller.NavigationController;
import vn.edu.auction.client.navigation.Navigator;

public class BrowseController {
    private static Runnable refreshCurrent = () -> {};
    public static void refreshCurrentAccount() { refreshCurrent.run(); }
    @FXML private Button accountButton;
    @FXML private Button manageButton;
    @FXML private Label accountLabel;
    @FXML private void initialize() { refreshCurrent = this::refreshAccount; refreshAccount(); }
    private void refreshAccount() {
        SessionData session = SessionData.getInstance();
        accountLabel.setText(session.isLoggedIn() ? "Xin chào, " + session.getFullName() : "Bạn đang xem với tư cách khách");
        accountButton.setText(session.isLoggedIn() ? "Đăng xuất" : "Đăng nhập");
        manageButton.setVisible(session.isLoggedIn());
        manageButton.setManaged(session.isLoggedIn());
    }
    @FXML private void handleManageItems() {
        if (!SessionData.getInstance().isLoggedIn()) return;
        NavigationController.showView("/vn/edu/auction/client/fxml/item-management-view.fxml", "Quản lý sản phẩm");
    }
    @FXML private void handleAccount() {
        if (SessionData.getInstance().isLoggedIn()) {
            SessionData.getInstance().clearSession();
            refreshAccount();
        } else {
            openLogin((Stage) accountButton.getScene().getWindow(), this::refreshAccount);
        }
    }
    public static void openLogin(Stage owner, Runnable onClose) {
        try {
            FXMLLoader loader = new FXMLLoader(BrowseController.class.getResource("/vn/edu/auction/client/fxml/auth/login-view.fxml"), I18n.getBundle());
            Parent root = loader.load();
            Stage popup = new Stage();
            popup.initOwner(owner);
            popup.initModality(Modality.WINDOW_MODAL);
            popup.setTitle("Đăng nhập");
            popup.getProperties().put("auction.auth.popup", Boolean.TRUE);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(BrowseController.class.getResource("/vn/edu/auction/client/css/auth.css").toExternalForm());
            vn.edu.auction.client.ui.UiTheme.apply(scene);
            popup.setScene(scene);
            popup.setResizable(false);
            popup.addEventHandler(KeyEvent.KEY_PRESSED, e -> { if (e.getCode() == KeyCode.ESCAPE) { popup.close(); e.consume(); } });
            popup.setOnHidden(e -> {
                Object callback = popup.getProperties().remove("auction.google.callback");
                if (callback instanceof com.sun.net.httpserver.HttpServer server) server.stop(0);
                Stage primary = owner;
                while (primary.getOwner() instanceof Stage parent) primary = parent;
                Navigator.setStage(primary); onClose.run(); });
            popup.show();
        } catch (IOException | RuntimeException e) {
            new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR, "Không thể mở đăng nhập. Vui lòng thử lại.").showAndWait();
        }
    }
}
