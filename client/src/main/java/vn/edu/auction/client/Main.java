
package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        URL resource = getClass().getResource("/vn/edu/auction/client/login-view.fxml");

        if (resource == null) {
            throw new IllegalStateException("Không tìm thấy login-view.fxml");
        }

        Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);

        URL css = getClass().getResource("/vn/edu/auction/client/auth.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setTitle("Tech Auction - Đăng nhập");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

