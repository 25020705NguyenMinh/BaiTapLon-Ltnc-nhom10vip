package vn.edu.auction.client;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

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
    //    Một controller dùng chung cho nhiều trang, trang nào không có
    //    fx:id đó thì biến sẽ là null -> nên kiểm tra null trước khi dùng.
    // ====================================================================

    // MỚI: chọn ngôn ngữ
    @FXML private ComboBox<String> languageComboBox;

    // email + lỗi dưới ô email (MỚI: emailError)
    @FXML private TextField emailField;
    @FXML private Label emailError;

    // mật khẩu + lỗi dưới ô mật khẩu (MỚI: passwordContainer, passwordError)
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordVisibleField;
    @FXML private HBox passwordContainer;
    @FXML private Label passwordError;
    @FXML private Button togglePasswordButton;
    private boolean passwordVisible = false;

    // MỚI: trang đăng ký (Họ/Tên thay cho fullNameField, bỏ roleComboBox)
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

    // ====================================================================
    // 2. KHỞI TẠO (chạy 1 lần mỗi khi một trang được mở)
    // ====================================================================
    @FXML
    public void initialize() {
        if (messageLabel != null) {
            messageLabel.setText("");
        }

        setupLanguageComboBox();

        // Điền sẵn email (trang 2 nhớ email, trang 3 hiển thị email đã nhập)
        if (emailField != null) {
            emailField.setText(RegisterData.email);
            emailField.textProperty().addListener(
                    (obs, oldValue, newValue) -> clearFieldError(emailError, emailField));
        }

        // Ô mật khẩu ẩn và ô mật khẩu hiện luôn có cùng nội dung
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

        // Gõ lại thì xóa lỗi đỏ
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
    // 3. CHỌN NGÔN NGỮ (MỚI)
    // ====================================================================
    private void setupLanguageComboBox() {
        if (languageComboBox == null) {
            return;
        }
        languageComboBox.getItems().setAll(LANG_VI, LANG_EN);
        languageComboBox.setValue(I18n.isVietnamese() ? LANG_VI : LANG_EN);
        // setOnAction đặt SAU setValue để lần đặt giá trị đầu không bị kích hoạt
        languageComboBox.setOnAction(e -> handleLanguageChange());
    }

    private void handleLanguageChange() {
        Locale selected = LANG_EN.equals(languageComboBox.getValue())
                ? Locale.ENGLISH
                : I18n.VIETNAMESE;

        if (selected.getLanguage().equals(I18n.getLocale().getLanguage())) {
            return; // không đổi gì thì thôi
        }

        I18n.setLocale(selected);

        // runLater: đợi ComboBox đóng xong rồi mới load lại trang
        Platform.runLater(() -> {
            try {
                Navigator.reload();
            } catch (IOException e) {
                showMessage(I18n.get("error.openView") + ": " + e.getMessage());
            }
        });
    }

    // ====================================================================
    // 4. ĐĂNG NHẬP (SỬA: báo lỗi ngay dưới từng ô)
    // ====================================================================
    @FXML
    private void handleLogin() {
        showMessage("");

        // Kiểm tra cả 2 ô rồi mới return để hiện lỗi cùng lúc
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

    // Các nút đăng nhập mạng xã hội: TỐI NAY BẠN THÊM CODE VÀO 3 HÀM NÀY
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
    // 5. ĐĂNG KÝ - 2 TRANG (MỚI). Vai trò luôn mặc định là BUYER.
    // ====================================================================

    // ---- Trang 1: chọn cách đăng ký (Google / Facebook / LinkedIn / email) ----
    @FXML
    private void handleEmailContinue() {
        showMessage("");
        if (!validateEmail()) {
            return;
        }
        RegisterData.email = emailField.getText().trim();
        openRegisterView();
    }

    // ---- Trang 2: thiết lập tài khoản ----
    @FXML
    private void handleRegister() {
        showMessage("");

        boolean firstOk = validateRequired(firstNameField, firstNameError, "error.firstName.empty");
        boolean lastOk = validateRequired(lastNameField, lastNameError, "error.lastName.empty");
        boolean passwordOk = validatePassword(true);

        if (!firstOk || !lastOk || !passwordOk) {
            return;
        }

        // Ô trái + ô phải ghép lại thành họ tên đầy đủ
        // (tiếng Việt: "Nguyễn Văn" + "An", tiếng Anh: "John" + "Smith")
        String fullName = firstNameField.getText().trim() + " " + lastNameField.getText().trim();
        String email = RegisterData.email;
        String role = RegisterData.role;
        String password = passwordField.getText();

        // TODO giai đoạn 3: gọi API đăng ký với fullName, email, password, role
        showMessage(I18n.get("message.api.pending"));
    }

    // ====================================================================
    // 6. QUÊN MẬT KHẨU (giữ nguyên, chỉ đổi sang lấy chữ từ I18n)
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
    // 7. CHUYỂN TRANG
    // ====================================================================
    @FXML
    private void openLoginView() {
        RegisterData.reset(); // thoát khỏi luồng đăng ký thì xóa dữ liệu tạm
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

    // SỬA: giờ chỉ gọi Navigator
    private void switchView(String fxmlFile, String titleKey) {
        try {
            Navigator.show(fxmlFile, titleKey);
        } catch (IOException e) {
            showMessage(I18n.get("error.openView") + ": " + e.getMessage());
        }
    }

    // ====================================================================
    // 8. ẨN / HIỆN MẬT KHẨU (SỬA: gọn hơn vì 2 ô đã bind với nhau)
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
    // 9. HÀM HỖ TRỢ KIỂM TRA + HIỆN LỖI DƯỚI Ô NHẬP (MỚI)
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

    /** checkLength = true khi đăng ký (yêu cầu tối thiểu 6 ký tự), false khi đăng nhập. */
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

    /** Hiện chữ đỏ dưới ô + viền đỏ quanh ô. */
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

    // Thông báo chung (ví dụ lỗi từ server) - nằm phía trên nút chính
    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    // ====================================================================
    // 10. TẢI ICON SVG (giữ nguyên code cũ của bạn)
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