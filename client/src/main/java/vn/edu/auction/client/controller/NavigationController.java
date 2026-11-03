package vn.edu.auction.client.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vn.edu.auction.client.model.SessionData;

import java.io.IOException;

/**
 * Bộ điều hướng dùng chung cho toàn bộ ứng dụng Client[cite: 3].
 * Quản lý việc chuyển đổi Scene trên Stage chính thay vì tạo Stage mới tùy tiện[cite: 3].
 */
public class NavigationController {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    /**
     * Tải file FXML và hiển thị lên giao diện chính[cite: 2].
     */
    public static void showView(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationController.class.getResource(fxmlPath));
            Parent root = loader.load();
            
            if (primaryStage != null) {
                Scene scene = primaryStage.getScene();
                if (scene == null) {
                    scene = new Scene(root);
                    primaryStage.setScene(scene);
                } else {
                    scene.setRoot(root);
                }
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

    /**
     * Chuyển sang màn hình Đăng nhập[cite: 2].
     */
    public static void showLogin() {
        showView("/vn/edu/auction/client/fxml/login-view.fxml", "Đăng Nhập");
    }
}