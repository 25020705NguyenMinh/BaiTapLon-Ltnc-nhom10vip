package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import vn.edu.auction.client.navigation.Navigator;

import java.io.IOException;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Lưu Stage chính vào Navigator để quản lý điều hướng tập trung
        Navigator.setPrimaryStage(primaryStage);

        try {
            // Khởi chạy ban đầu trực tiếp vào Trang Chủ (Home View)
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/vn/edu/auction/client/fxml/home/home-view.fxml")
            );
            Parent root = loader.load();

            Scene scene = new Scene(root, 1000, 700);

            // Nạp CSS chung của ứng dụng nếu có
            String cssPath = getClass().getResource("/vn/edu/auction/client/css/app.css") != null
                    ? getClass().getResource("/vn/edu/auction/client/css/app.css").toExternalForm()
                    : null;
            if (cssPath != null) {
                scene.getStylesheets().add(cssPath);
            }

            primaryStage.setTitle("Hệ Thống Đấu Giá Trực Tuyến - Trang Chủ");
            primaryStage.setScene(scene);
            primaryStage.show();

        } catch (IOException e) {
            System.err.println("Lỗi khi tải giao diện Trang Chủ:");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}