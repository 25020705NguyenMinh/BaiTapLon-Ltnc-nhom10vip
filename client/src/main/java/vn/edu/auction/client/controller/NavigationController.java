package vn.edu.auction.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import vn.edu.auction.client.I18n;
/**
 * Bộ điều hướng dùng chung cho toàn bộ ứng dụng Client
 * Quản lý việc chuyển đổi Scene trên Stage chính thay vì tạo Stage mới tùy tiện
 */
public class NavigationController {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Tải file FXML và hiển thị lên giao diện chính
     */
    public static void showView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath), I18n.getBundle());
            Parent root = loader.load();
            
            if (primaryStage != null) {
                Scene scene = primaryStage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    primaryStage.setScene(scene);
                } else {
                    scene.setRoot(root);
                }
                scene.getStylesheets().clear();
                if (fxmlPath.contains("/fxml/browse/")) {
                    scene.getStylesheets().add(NavigationController.class.getResource("/vn/edu/auction/client/css/browse.css").toExternalForm());
                } else if (fxmlPath.contains("/fxml/auth/")) {
                    scene.getStylesheets().add(NavigationController.class.getResource("/vn/edu/auction/client/css/auth.css").toExternalForm());
                }
                vn.edu.auction.client.ui.UiTheme.apply(scene);
                primaryStage.setTitle(title);
                primaryStage.show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể tải view FXML: " + fxmlPath);
        }
    }

    /**
     * Chuyển sang màn hình Trang chủ[cite: 2].
     */
    public static void showHome() {
        showView("/vn/edu/auction/client/fxml/home-view.fxml", "Trang Chủ - Hệ Thống Đấu Giá");
    }

    public static void showBrowse() {
        showView("/vn/edu/auction/client/fxml/browse/browse-view.fxml", "Sàn đấu giá | Khám phá");
    }

    public static void showProducts() {
        showBrowse();
        if (primaryStage != null && primaryStage.getScene() != null
                && primaryStage.getScene().getRoot() instanceof javafx.scene.layout.BorderPane pane
                && pane.getCenter() instanceof javafx.scene.control.TabPane tabs
                && tabs.getTabs().size() > 1) {
            tabs.getSelectionModel().select(1);
        }
    }

    /**
     * Chuyển sang màn hình Đăng nhập[cite: 2].
     */
    public static void showLogin() {
        showView("/vn/edu/auction/client/fxml/auth/login-view.fxml", I18n.get("title.login"));
    }
}
