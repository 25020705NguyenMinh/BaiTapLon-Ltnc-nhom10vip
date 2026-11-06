package vn.edu.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Lấy đường dẫn đến file login-view.fxml
        URL fxmlUrl = Main.class.getResource(
                "/vn/edu/auction/client/fxml/auth/login-view.fxml"
        );

        // Tải giao diện từ FXML
        FXMLLoader loader = new FXMLLoader(
                fxmlUrl,
                I18n.getBundle()
        );  // load cả file fxml và file bundle
        Parent root = loader.load();

        // Tạo Scene chứa giao diện
        Scene scene = new Scene(root);

        // Nạp CSS
        URL cssUrl = Main.class.getResource(
                "/vn/edu/auction/client/css/auth.css"
        );

        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        // Thiết lập cửa sổ
        stage.setTitle("Online Auction System");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}