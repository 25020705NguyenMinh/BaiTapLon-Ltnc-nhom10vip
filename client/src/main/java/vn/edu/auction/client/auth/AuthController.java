package vn.edu.auction.client.auth;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.Window;
import vn.edu.auction.client.I18n;
import vn.edu.auction.client.navigation.Navigator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Pattern;

public class AuthController {

    // ====================================================================
    // 1. KHAI BÁO CÁC THÀNH PHẦN TRONG FXML (fx:id)
    // ====================================================================
    @FXML private ComboBox<String> languageComboBox;

    @FXML private TextField emailField;
    @FXML private Label emailError;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private HBox passwordContainer;
    @FXML private Label passwordError;
    @FXML private Button togglePasswordButton;
    private boolean passwordVisible = false;

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;

    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;

    private static final String LANG_VI = "Tiếng Việt";
    private static final String LANG_EN = "English";

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");

    // ---- SỬA: cấu hình chuyển trang ----
    // Thư mục chứa các file FXML của phần auth (tính từ /vn/edu/auction/client/)
    private static final String VIEW_DIR = "fxml/auth/";
    // Đường dẫn CSS (Navigator đang tìm sai chỗ nên controller tự gắn)
    private static final String AUTH_CSS = "/vn/edu/auction/client/css/auth.css";

    // Nhớ trang hiện tại để đổi ngôn ngữ thì load lại đúng trang.
    // Mặc định là login vì Main mở trang login đầu tiên.
    private static String currentFxml = "login-view.fxml";
    private static String currentTitleKey = "title.login";

    // ====================================================================
    // 2. KHỞI TẠO
    // ====================================================================
    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }

        setupLanguageComboBox();

        if (emailField != null) {
            emailField.setText(RegisterData.email);
            emailField.textProperty().addListener(
                    (obs, oldValue, newValue) -> clearFieldError(emailError, emailField));
        }

        if (passwordField != null && passwordVisibleField != null) {
            passwordVisibleField.textProperty().bindBidirectional(passwordField.textProperty());
        }
        if (passwordField != null) {
            passwordField.textProperty().addListener(
                    (obs, oldValue, newValue) -> clearFieldError(passwordError, passwordContainer));
        }
        if (togglePasswordButton != null) {
            togglePasswordButton.setGraphic(loadSvgIcon("eye.svg"));
        }

        if (firstNameField != null) {
            firstNameField.textProperty().addListener(
                    (obs, oldValue, newValue) -> clearFieldError(firstNameError, firstNameField));
        }
        if (lastNameField != null) {
            lastNameField.textProperty().addListener(
                    (obs, oldValue, newValue) -> clearFieldError(lastNameError, lastNameField));
        }
    }

    // ====================================================================
    // 3. CHỌN NGÔN NGỮ
    // ====================================================================
    private void setupLanguageComboBox() {
        if (languageComboBox == null) {
            return;
        }
        languageComboBox.getItems().setAll(LANG_VI, LANG_EN);
        languageComboBox.setValue(I18n.isVietnamese() ? LANG_VI : LANG_EN);
        languageComboBox.setOnAction(e -> handleLanguageChange());
    }

    private void handleLanguageChange() {
        Locale selected = LANG_EN.equals(languageComboBox.getValue())
                ? Locale.ENGLISH
                : I18n.VIETNAMESE;

        if (selected.getLanguage().equals(I18n.getLocale().getLanguage())) {
            return;
        }

        I18n.setLocale(selected);

        // SỬA: không dùng Navigator.reload() nữa (trang login do Main mở nên
        // Navigator chưa biết trang hiện tại). Tự load lại trang đang đứng.
        Platform.runLater(() -> switchView(currentFxml, currentTitleKey));
    }

    // ====================================================================
    // 4. ĐĂNG NHẬP
    // ====================================================================
    @FXML
    private void handleLogin() {
        showMessage("");

        boolean emailOk = validateEmail();
        boolean passwordOk = validatePassword(false);

        if (!emailOk || !passwordOk) {
            return;
        }

        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // TODO giai đoạn 3: gọi API đăng nhập bằng email + password
        showMessage(I18n.get("message.api.pending"));
    }

    @FXML
    private void handleGoogleLogin() {
        // TODO: code đăng nhập Google
        showMessage(I18n.get("message.social.pending"));
    }

    @FXML
    private void handleFacebookLogin() {
        // TODO: code đăng nhập Facebook
        showMessage(I18n.get("message.social.pending"));
    }

    @FXML
    private void handleLinkedinLogin() {
        // TODO: code đăng nhập LinkedIn
        showMessage(I18n.get("message.social.pending"));
    }

    // ====================================================================
    // 5. ĐĂNG KÝ
    // ====================================================================
    @FXML
    private void handleEmailContinue() {
        showMessage("");
        if (!validateEmail()) {
            return;
        }
        RegisterData.email = emailField.getText().trim();
        openRegisterView();
    }

    @FXML
    private void handleRegister() {
        showMessage("");

        boolean firstOk = validateRequired(firstNameField, firstNameError, "error.firstName.empty");
        boolean lastOk = validateRequired(lastNameField, lastNameError, "error.lastName.empty");
        boolean passwordOk = validatePassword(true);

        if (!firstOk || !lastOk || !passwordOk) {
            return;
        }

        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String email = RegisterData.email;
        String role = RegisterData.role;
        String password = passwordField.getText();

        // TODO giai đoạn 3: gọi API đăng ký với fullName, email, password, role
        showMessage(I18n.get("message.api.pending"));
    }

    // ====================================================================
    // 6. QUÊN MẬT KHẨU
    // ====================================================================
    @FXML
    private void handleConfirmReset() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage(I18n.get("error.email.empty"));
            return;
        }
        showMessage(I18n.get("message.api.pending"));
    }

    // ====================================================================
    // 7. CHUYỂN TRANG (ĐÃ SỬA)
    //    Chỉ truyền tên file, VIEW_DIR sẽ tự thêm "fxml/auth/" phía trước.
    // ====================================================================
    @FXML
    private void openLoginView() {
        RegisterData.reset();
        switchView("login-view.fxml", "title.login");
    }

    @FXML
    private void openSignupMethodView() {
        switchView("signup-method-view.fxml", "title.signupMethod");
    }

    @FXML
    private void openRegisterView() {
        switchView("register-view.fxml", "title.register");
    }

    @FXML
    private void openResetPasswordView() {
        switchView("reset-password-view.fxml", "title.reset");
    }

    private void switchView(String fxmlFile, String titleKey) {
        try {
            // 1) Lấy cửa sổ hiện tại và đưa cho Navigator (Main chưa làm việc này)
            Stage stage = findStage();
            if (stage == null) {
                throw new IOException("Không tìm thấy cửa sổ hiện tại");
            }
            Navigator.setStage(stage);

            // 2) Mở trang với đường dẫn đầy đủ: fxml/auth/xxx.fxml
            Navigator.show(VIEW_DIR + fxmlFile, titleKey);

            // 3) Navigator tìm CSS sai chỗ -> tự gắn CSS đúng vào scene mới
            applyAuthCss(stage.getScene());

            // 4) Nhớ trang hiện tại cho lần đổi ngôn ngữ
            currentFxml = fxmlFile;
            currentTitleKey = titleKey;
        } catch (IOException e) {
            showMessage(I18n.get("error.openView") + ": " + e.getMessage());
        }
    }

    /** Lấy Stage từ bất kỳ ô nào đang có trên trang hiện tại. */
    private Stage findStage() {
        Node[] nodes = {
                emailField, passwordField, firstNameField, lastNameField,
                languageComboBox, messageLabel, loginButton, registerButton
        };
        for (Node node : nodes) {
            if (node != null && node.getScene() != null) {
                Window window = node.getScene().getWindow();
                if (window instanceof Stage stage) {
                    return stage;
                }
            }
        }
        return null;
    }

    private void applyAuthCss(Scene scene) {
        if (scene == null) {
            return;
        }
        var css = getClass().getResource(AUTH_CSS);
        if (css != null && !scene.getStylesheets().contains(css.toExternalForm())) {
            scene.getStylesheets().add(css.toExternalForm());
        }
    }

    // ====================================================================
    // 8. ẨN / HIỆN MẬT KHẨU
    // ====================================================================
    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);

        passwordVisibleField.setVisible(passwordVisible);
        passwordVisibleField.setManaged(passwordVisible);

        togglePasswordButton.setGraphic(loadSvgIcon(passwordVisible ? "eye-off.svg" : "eye.svg"));

        TextField shown = passwordVisible ? passwordVisibleField : passwordField;
        shown.requestFocus();
        shown.positionCaret(shown.getText().length());
    }

    // ====================================================================
    // 9. KIỂM TRA + HIỆN LỖI DƯỚI Ô NHẬP
    // ====================================================================
    private boolean validateEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showFieldError(emailError, emailField, I18n.get("error.email.empty"));
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            showFieldError(emailError, emailField, I18n.get("error.email.invalid"));
            return false;
        }
        return true;
    }

    /** checkLength = true khi đăng ký (tối thiểu 6 ký tự), false khi đăng nhập. */
    private boolean validatePassword(boolean checkLength) {
        String password = passwordField.getText();

        if (password.isEmpty()) {
            showFieldError(passwordError, passwordContainer, I18n.get("error.password.empty"));
            return false;
        }
        if (checkLength && password.length() < 6) {
            showFieldError(passwordError, passwordContainer, I18n.get("error.password.short"));
            return false;
        }
        return true;
    }

    private boolean validateRequired(TextField field, Label errorLabel, String messageKey) {
        if (field.getText().trim().isEmpty()) {
            showFieldError(errorLabel, field, I18n.get(messageKey));
            return false;
        }
        return true;
    }

    private void showFieldError(Label errorLabel, Node field, String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true);
        }
        if (field != null && !field.getStyleClass().contains("input-error")) {
            field.getStyleClass().add("input-error");
        }
    }

    private void clearFieldError(Label errorLabel, Node field) {
        if (errorLabel != null) {
            errorLabel.setText("");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
        }
        if (field != null) {
            field.getStyleClass().remove("input-error");
        }
    }

    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    // ====================================================================
    // 10. TẢI ICON SVG
    // ====================================================================
    private ImageView loadSvgIcon(String fileName) {
        try {
            var url = getClass().getResource("/vn/edu/auction/client/icons/" + fileName);

            if (url == null) {
                throw new IllegalArgumentException("Không tìm thấy icon: " + fileName);
            }

            SVGDocument document = new SVGLoader().load(url);
            var size = document.size();

            BufferedImage image = new BufferedImage(
                    Math.max(1, (int) size.width),
                    Math.max(1, (int) size.height),
                    BufferedImage.TYPE_INT_ARGB
            );

            var graphics = image.createGraphics();
            document.render(null, graphics);
            graphics.dispose();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);

            ImageView icon = new ImageView(
                    new Image(new ByteArrayInputStream(output.toByteArray()))
            );

            icon.setFitWidth(18);
            icon.setFitHeight(18);
            return icon;

        } catch (Exception e) {
            throw new RuntimeException("Không thể tải icon SVG: " + fileName, e);
        }
    }
}