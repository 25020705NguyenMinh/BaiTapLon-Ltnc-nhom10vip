package vn.edu.auction.client.navigation;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class Navigator {

    private static Stage primaryStage;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void setStage(Stage stage) {
        setPrimaryStage(stage);
    }

    public static void show() {
        if (primaryStage != null && !primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    public static void show(String fxmlPath) {
        navigateTo(fxmlPath);
    }

    public static void show(String fxmlPath, String titleKey) {
        navigateTo(fxmlPath);
        if (primaryStage != null && titleKey != null && !titleKey.isEmpty()) {
            primaryStage.setTitle(titleKey);
        }
    }

    public static void showView(String fxmlPath) {
        navigateTo(fxmlPath);
    }

    /**
     * Chuyển đổi màn hình thông qua đường dẫn FXML (đã hỗ trợ nạp ResourceBundle i18n)
     */
    public static void navigateTo(String fxmlPath) {
        if (primaryStage == null) {
            System.err.println("PrimaryStage chưa được khởi tạo trong Navigator!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(Navigator.class.getResource(fxmlPath));

            // Nạp file ngôn ngữ mặc định (messages_vi.properties) để tránh lỗi "No resources specified"
            try {
                ResourceBundle bundle = ResourceBundle.getBundle(
                        "vn.edu.auction.client.i18n.messages",
                        Locale.forLanguageTag("vi-VN")
                );
                loader.setResources(bundle);
            } catch (Exception e) {
                System.out.println("Cảnh báo: Không thể tải file i18n ResourceBundle, tiếp tục không có bundle.");
            }

            Parent root = loader.load();

            if (primaryStage.getScene() != null) {
                primaryStage.getScene().setRoot(root);
            } else {
                primaryStage.setScene(new Scene(root, 1000, 700));
            }

            show();

        } catch (IOException e) {
            System.err.println("Không thể chuyển sang màn hình: " + fxmlPath);
            e.printStackTrace();
        }
    }

    // --- CÁC ĐƯỜNG DẪN MÀN HÌNH CHUẨN TRONG DỰ ÁN ---
    public static final String HOME_VIEW = "/vn/edu/auction/client/fxml/home/home-view.fxml";
    public static final String LOGIN_VIEW = "/vn/edu/auction/client/fxml/auth/login-view.fxml";
    public static final String REGISTER_VIEW = "/vn/edu/auction/client/fxml/auth/register-view.fxml";
    public static final String BROWSE_VIEW = "/vn/edu/auction/client/fxml/browse/browse-view.fxml";
    public static final String AUCTION_DETAIL_VIEW = "/vn/edu/auction/client/fxml/auction/auction-detail-view.fxml";
    public static final String BID_HISTORY_VIEW = "/vn/edu/auction/client/fxml/auction/bid-history-view.fxml";
    public static final String AUCTION_RESULT_VIEW = "/vn/edu/auction/client/fxml/auction/auction-result-view.fxml";
}