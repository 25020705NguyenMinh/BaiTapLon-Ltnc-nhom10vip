package vn.edu.auction.client.auth;


import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
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
import javafx.util.Duration;
import vn.edu.auction.client.I18n;
import vn.edu.auction.client.api.AuthApiClient;
import vn.edu.auction.client.controller.NavigationController;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.client.navigation.Navigator;
import vn.edu.auction.shared.dto.AuthResponse;
import vn.edu.auction.shared.dto.LoginRequest;
import vn.edu.auction.shared.dto.RegisterRequest;
import vn.edu.auction.shared.enums.UserRole;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.concurrent.CompletableFuture.runAsync;

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
    // [3] Màu chữ thông báo thành công (mặc định messageLabel màu đỏ trong CSS)
    private static final String SUCCESS_STYLE = "-fx-text-fill: #16a34a;";

    // [3] Dùng System.Logger để ghi chi tiết lỗi ra CONSOLE cho lập trình viên,
    // còn giao diện chỉ hiện câu thông báo dễ hiểu (không hiện stack trace).
    private static final System.Logger LOG = System.getLogger(AuthController.class.getName());

    // [3] static final: dùng chung 1 đối tượng cho mọi màn hình auth.
    // VÌ SAO: mỗi lần chuyển trang JavaFX tạo controller mới; nếu `new AuthApiClient()` trong
    // controller thì mỗi lần lại tạo thêm 1 HttpClient (tốn luồng và bộ nhớ).
    private static final AuthApiClient AUTH_API = new AuthApiClient();

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

        // Dựng request ngay trên luồng giao diện (đọc ô nhập chỉ được làm ở đây);
        // luồng nền chỉ nhận request đã dựng sẵn.
        LoginRequest request = new LoginRequest(
                emailField.getText().trim(),
                passwordField.getText());

        runAsync(() -> {
            try {
                return AUTH_API.login(request);
            } catch (AuthApiClient.ApiException e) {
                throw new RuntimeException(e);
            }
        }, this::onLoginSuccess);
    }
    private void onLoginSuccess(AuthResponse response) {
        Stage stage = findStage();
        if (stage == null) {
            showMessage(I18n.get("error.openView"));
            return;
        }
        // 1) Lưu AuthResponse (có token) vào phiên để các màn hình sau dùng gọi API.
        SessionData.getInstance().setSession(response);

        // 2) Xóa mật khẩu khỏi ô nhập. VÌ SAO: không để mật khẩu nằm lại trong bộ nhớ giao diện
        //    lâu hơn cần thiết. Mật khẩu cũng không được lưu ở SessionData hay RegisterData.
        passwordField.clear();
        RegisterData.reset();

        // 3) Báo cho các bộ điều hướng biết cửa sổ chính, rồi chuyển tới trang chủ.
        //    VÌ SAO set cả hai: Main chưa gọi setStage/setPrimaryStage; thiếu bước này
        //    showHome() không làm gì vì stage = null, và đăng xuất cũng hỏng theo.
        Navigator.setStage(stage);
        NavigationController.setPrimaryStage(stage);
        NavigationController.showHome();
    }
    @FXML
    private void handleGoogleLogin() {
        // TODO: code đăng nhập Google
        showMessage(I18n.get("message.social.pending"));
    }
    /** Chạy trên luồng giao diện sau khi server báo đăng nhập thành công. */

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
        String email = RegisterData.email.trim();

        // Dùng đúng RegisterRequest trong module shared để gửi cho server.
        RegisterRequest request = new RegisterRequest(
                fullName,
                email,
                passwordField.getText(),
                parseRole(RegisterData.role));

        runAsync(() -> {
            try {
                return AUTH_API.register(request);
            } catch (AuthApiClient.ApiException e) {
                throw new RuntimeException(e);
            }
        }, response -> onRegisterSuccess(email));
    }

    /**
     * Đăng ký xong: hiện thông báo thành công, rồi chuyển về trang đăng nhập.
     * VÌ SAO không tự đăng nhập luôn dù server có trả token: đề yêu cầu "hiển thị thành công";
     * để người dùng tự đăng nhập là luồng quen thuộc, và token chỉ được lưu ở một chỗ (handleLogin).
     */
    private void onRegisterSuccess(String email) {
        passwordField.clear();
        showSuccess(I18n.get("message.register.success"));

        // Khóa nút trong lúc chờ chuyển trang để người dùng không bấm đăng ký lần 2 (sẽ bị 409).
        registerButton.setDisable(true);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));
        pause.setOnFinished(e -> {
            RegisterData.reset();
            // Giữ lại email để trang đăng nhập điền sẵn (initialize() đọc RegisterData.email)
            RegisterData.email = email;
            switchView("login-view.fxml", "title.login");
        });
        pause.play();
    }

    // RegisterData.role là chuỗi. Chuyển sang enum của shared.
    // VÌ SAO có nhánh dự phòng: nếu chuỗi lạ , vẫn đăng ký được với vai trò
    // mặc định BIDDER thay vì văng lỗi. Server cũng cấm tự đăng ký ADMIN.
    private UserRole parseRole(String role) {
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException | NullPointerException e) {
            return UserRole.BIDDER;
        }
    }

    // ====================================================================
    // 6. GỌI API Ở LUỒNG NỀN  [MỚI]
    // ====================================================================
    /**
     * Chạy lời gọi API ở luồng riêng rồi trả kết quả về luồng giao diện.
     *
     * VÌ SAO KHÔNG GỌI TRỰC TIẾP: gọi mạng chặn luồng JavaFX tới 10 giây (khi server chậm hoặc
     * tắt) thì cửa sổ đứng hình. Task chạy nền và gọi lại trên luồng giao diện, nên an toàn
     * khi cập nhật Label/Scene.
     */
    private void runAsync(Callable<AuthResponse> apiCall, Consumer<AuthResponse> onSuccess) {
        setBusy(true);
        showMessage(I18n.get("message.loading"));

        Task<AuthResponse> task = new Task<>() {
            @Override
            protected AuthResponse call() throws Exception {
                return apiCall.call();
            }
        };

        task.setOnSucceeded(e -> {
            setBusy(false);
            showMessage("");
            onSuccess.accept(task.getValue());
        });

        task.setOnFailed(e -> {
            setBusy(false);
            showMessage(describeError(task.getException()));
        });

        Thread thread = new Thread(task, "auth-api-call");
        thread.setDaemon(true); // đóng cửa sổ là luồng này tự dừng, không giữ app lại
        thread.start();
    }

    // Khóa nút trong lúc chờ server, chống bấm nhiều lần
    private void setBusy(boolean busy) {
        if (loginButton != null) {
            loginButton.setDisable(busy);
        }
        if (registerButton != null) {
            registerButton.setDisable(busy);
        }
    }

    /**
     * Đổi lỗi kỹ thuật thành câu dễ hiểu.
     * - Chi tiết kỹ thuật (stack trace) chỉ ghi ra console.
     * - Người dùng chỉ thấy câu thông báo lấy từ file i18n (có cả tiếng Việt và tiếng Anh).
     */
    private String describeError(Throwable error) {
        LOG.log(System.Logger.Level.WARNING, "Gọi API xác thực thất bại", error);

        if (error instanceof AuthApiClient.ApiException apiError) {
            return switch (apiError.getType()) {
                // 400: câu của server cụ thể hơn (vd. "Mật khẩu phải có ít nhất 6 ký tự")
                case BAD_REQUEST -> apiError.getServerMessage() != null
                        ? apiError.getServerMessage()
                        : I18n.get("error.badRequest");
                case UNAUTHORIZED -> I18n.get("error.login.invalid");
                case FORBIDDEN -> I18n.get("error.forbidden");
                case CONFLICT -> I18n.get("error.email.exists");
                case SERVER -> I18n.get("error.server");
                case NETWORK -> I18n.get("error.network");
                case TIMEOUT -> I18n.get("error.timeout");
                case UNKNOWN -> I18n.get("error.unknown");
            };
        }
        return I18n.get("error.unknown");
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
            // [SỬA] Ghi chi tiết ra console, giao diện chỉ hiện câu chung (không lộ đường dẫn file)
            LOG.log(System.Logger.Level.ERROR, "Không mở được màn hình " + fxmlFile, e);
            showMessage(I18n.get("error.openView"));
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

    // [SỬA] Thông báo lỗi: trả về kiểu mặc định (đỏ theo CSS)
    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle("");
            messageLabel.setText(message);
        }
    }

    // [MỚI] Thông báo thành công: đổi sang màu xanh lá
    private void showSuccess(String message) {
        if (messageLabel != null) {
            messageLabel.setStyle(SUCCESS_STYLE);
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