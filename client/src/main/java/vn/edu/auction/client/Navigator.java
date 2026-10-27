package vn.edu.auction.client;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * FILE MỚI - chuyển giữa các màn hình (thay cho hàm switchView cũ trong AuthController).
 * Mỗi lần load FXML đều truyền ResourceBundle để FXML dùng được cú pháp %key.
 */
public final class Navigator {

    private static final String BASE_PATH = "/vn/edu/auction/client/";

    private static Stage stage;
    private static String currentFxml;
    private static String currentTitleKey;

    private Navigator() {
    }

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void show(String fxmlFile, String titleKey) throws IOException {
        var resource = Navigator.class.getResource(BASE_PATH + fxmlFile);
        if (resource == null) {
            throw new IOException("Không tìm thấy giao diện: " + fxmlFile);
        }

        Parent root = FXMLLoader.load(resource, I18n.getBundle());
        Scene scene = new Scene(root);

        var css = Navigator.class.getResource(BASE_PATH + "auth.css");
        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        stage.setScene(scene);
        stage.setTitle(I18n.get(titleKey));
        stage.sizeToScene();
        stage.centerOnScreen();

        currentFxml = fxmlFile;
        currentTitleKey = titleKey;
    }

    /** Load lại màn hình hiện tại (dùng khi đổi ngôn ngữ). */
    public static void reload() throws IOException {
        show(currentFxml, currentTitleKey);
    }
}
