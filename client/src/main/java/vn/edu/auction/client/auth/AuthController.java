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
import javafx.util.StringConverter;
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
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import java.util.function.UnaryOperator;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

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
    @FXML private ComboBox<UserRole> roleComboBox;
    @FXML private Label firstNameError;
    @FXML private Label lastNameError;

    @FXML private Label messageLabel;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    // ---- Màn hình OTP (khớp fx:id trong otp-verification-view.fxml) ----
    @FXML private TextField otp1, otp2, otp3, otp4, otp5, otp6;
    @FXML private Label emailLabel;
    @FXML private Hyperlink resendLink;
    @FXML private Label countdownLabel;
    @FXML private Button continueButton;

    @FXML private PasswordField confirmPasswordField;
    @FXML private Label confirmPasswordError;
    @FXML private Button submitButton;
    private TextField[] otpBoxes;
    private Timeline resendTimer;
    private boolean otpFilling = false;          // true khi code đang tự điền ô (dán mã)
    private static final int RESEND_SECONDS = 60; // phải khớp app.otp.resend-cooldown-seconds của server

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
        if (roleComboBox != null) {
            roleComboBox.getItems().setAll(UserRole.BIDDER, UserRole.SELLER);
            roleComboBox.setConverter(new StringConverter<>() {
                @Override public String toString(UserRole role) {
                    return role == null ? "" : I18n.get("role." + role.name());
                }
                @Override public UserRole fromString(String value) { return null; }
            });
            roleComboBox.setValue(parseRole(RegisterData.role));
        }

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
        // Trang OTP: có ô otp1 thì cấu hình 6 ô nhập và đồng hồ gửi lại
        if (otp1 != null) {
            setupOtpView();
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

        runAsync(() -> AUTH_API.login(request), this::onLoginSuccess);
    }
    private void onLoginSuccess(AuthResponse response) {
        Stage stage = findStage();
        if (stage == null) {
            showMessage(I18n.get("error.openView"));
            return;
        }
        if (!stage.isShowing()) {
            return;
        }
        // 1) Lưu AuthResponse (có token) vào phiên để các màn hình sau dùng gọi API.
        SessionData.getInstance().setSession(response);

        // 2) Xóa mật khẩu khỏi ô nhập. VÌ SAO: không để mật khẩu nằm lại trong bộ nhớ giao diện
        //    lâu hơn cần thiết. Mật khẩu cũng không được lưu ở SessionData hay RegisterData.
        if (passwordField != null) {
            passwordField.clear();
        }
        RegisterData.reset();

        if (Boolean.TRUE.equals(stage.getProperties().get("auction.auth.popup"))) {
            stage.close();
            return;
        }

        // 3) Báo cho các bộ điều hướng biết cửa sổ chính, rồi chuyển tới trang chủ.
        //    VÌ SAO set cả hai: Main chưa gọi setStage/setPrimaryStage; thiếu bước này
        //    showHome() không làm gì vì stage = null, và đăng xuất cũng hỏng theo.
        Navigator.setStage(stage);
        NavigationController.setPrimaryStage(stage);
        NavigationController.showHome();
    }
    @FXML
    private void handleGoogleLogin() {
        showMessage("");

        // 1. Mở trạm thu sóng nội bộ tại port 8081 để hứng Token và Name từ Server
        startLocalCallbackServer();

        // 2. Mở trình duyệt gọi lên Spring Boot Server
        String googleAuthUrl = "https://baitaplon-ltnc-nhom10vip.onrender.com/oauth2/authorization/google";
        openOAuthBrowser(googleAuthUrl);
    }

    /**
     * Mở một HTTP Server tạm thời trên JavaFX (cổng 8081) để nhận Token và Name trả về.
     */
    private void startLocalCallbackServer() {
        try {
            com.sun.net.httpserver.HttpServer server = com.sun.net.httpserver.HttpServer.create(new java.net.InetSocketAddress(8081), 0);
            Stage authStage = findStage();
            if (authStage != null && Boolean.TRUE.equals(authStage.getProperties().get("auction.auth.popup"))) {
                authStage.getProperties().put("auction.google.callback", server);
            }

            server.createContext("/callback", exchange -> {
                try {
                    String query = exchange.getRequestURI().getQuery();
                    String token = null;
                    String name = "Người dùng"; // Tên mặc định nếu không lấy được

                    // Tách lấy token và name từ URL
                    if (query != null) {
                        for (String param : query.split("&")) {
                            if (param.startsWith("token=")) {
                                token = param.substring(6);
                            } else if (param.startsWith("name=")) {
                                // Giải mã tiếng Việt
                                name = java.net.URLDecoder.decode(param.substring(5), java.nio.charset.StandardCharsets.UTF_8);
                            }
                        }
                    }

                    // Trả về HTML báo cho người dùng tắt trình duyệt
                    String htmlResponse = "<html><head><title>Thành công</title></head>"
                            + "<body style='text-align:center; padding-top:50px; font-family:Arial;'>"
                            + "<h2 style='color:#4CAF50;'>Đăng nhập thành công!</h2>"
                            + "<p>Bạn có thể đóng cửa sổ này và quay lại ứng dụng Tech Auction.</p>"
                            + "</body></html>";

                    byte[] responseBytes = htmlResponse.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
                    exchange.sendResponseHeaders(200, responseBytes.length);
                    exchange.getResponseBody().write(responseBytes);

                    // Truyền cả token và name xuống cho giao diện
                    if (token != null && !token.isEmpty()) {
                        String finalToken = token;
                        String finalName = name;
                        javafx.application.Platform.runLater(() -> {
                            AuthResponse authResponse = new AuthResponse();
                            authResponse.setToken(finalToken);

                            // Gắn tên vào AuthResponse để hiển thị "Xin chào, [Tên]"
                            authResponse.setFullName(finalName);

                            onLoginSuccess(authResponse);
                        });
                    }
                } finally {
                    exchange.close();
                    server.stop(0); // Tắt server nội bộ ngay khi nhận xong
                }
            });

            server.start();
            System.out.println("Local Callback Server started on port 8081");

        } catch (Exception e) {
            e.printStackTrace();
            javafx.application.Platform.runLater(() -> showMessage("Lỗi khởi tạo trạm nhận đăng nhập."));
        }
    }
//
//    @FXML
//    private void handleFacebookLogin() {
//        // Link đăng nhập Facebook OAuth2 (Thay app_id bằng App ID từ Meta Developer)
//        String facebookAuthUrl = "https://www.facebook.com/v18.0/dialog/oauth"
//                + "?client_id=YOUR_FACEBOOK_APP_ID"
//                + "&redirect_uri=http://localhost:8080/login/oauth2/code/facebook"
//                + "&scope=email,public_profile";
//
//        openOAuthBrowser(facebookAuthUrl);
//    }
//
//    @FXML
//    private void handleLinkedinLogin() {
//        // Link đăng nhập LinkedIn OAuth2
//        String linkedinAuthUrl = "https://www.linkedin.com/oauth/v2/authorization"
//                + "?response_type=code"
//                + "&client_id=YOUR_LINKEDIN_CLIENT_ID"
//                + "&redirect_uri=http://localhost:8080/login/oauth2/code/linkedin"
//                + "&scope=openid%20profile%20email";
//
//        openOAuthBrowser(linkedinAuthUrl);
//    }

    /**
     * Hàm mở URL trang đăng nhập OAuth2 trên trình duyệt hệ thống (Chrome/Edge)
     */
    private void openOAuthBrowser(String url) {
        try {
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } else {
                // Dự phòng nếu hệ thống không hỗ trợ AWT Desktop
                Runtime runtime = Runtime.getRuntime();
                String os = System.getProperty("os.name").toLowerCase();
                if (os.contains("win")) {
                    runtime.exec("rundll32 url.dll,FileProtocolHandler " + url);
                } else if (os.contains("mac")) {
                    runtime.exec("open " + url);
                } else if (os.contains("nix") || os.contains("nux")) {
                    runtime.exec("xdg-open " + url);
                }
            }
        } catch (Exception e) {
            LOG.log(System.Logger.Level.ERROR, "Không thể mở trang đăng nhập mạng xã hội", e);
            showMessage("Không thể mở trình duyệt đăng nhập.");
        }
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
        UserRole selectedRole = roleComboBox == null || roleComboBox.getValue() == null
                ? UserRole.BIDDER : roleComboBox.getValue();
        RegisterData.role = selectedRole.name();

        // Dùng đúng RegisterRequest trong module shared để gửi cho server.
        RegisterRequest request = new RegisterRequest(
                fullName,
                email,
                passwordField.getText(),
                selectedRole);

        runAsync(() -> AUTH_API.register(request), response -> onRegisterSuccess(email));
    }

    /**
     * Đăng ký xong: hiện thông báo thành công, rồi chuyển về trang đăng nhập.
     * VÌ SAO không tự đăng nhập luôn dù server có trả token: đề yêu cầu "hiển thị thành công";
     * để người dùng tự đăng nhập là luồng quen thuộc, và token chỉ được lưu ở một chỗ (handleLogin).
     */
    /**
     * Đăng ký xong: server đã gửi mã OTP tới email, nên chuyển sang trang nhập mã.
     * Trang OTP đọc email từ RegisterData.email (đã được giữ lại).
     */
    private void onRegisterSuccess(String email) {
        if (passwordField != null) {
            passwordField.clear();
        }
        RegisterData.email = email;
        switchView("otp-verification-view.fxml", "title.otpMethod");
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
    private <T> void runAsync(Callable<T> apiCall, Consumer<T> onSuccess) {
        setBusy(true);
        showMessage(I18n.get("message.loading"));

        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
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
        thread.setDaemon(true);
        thread.start();
    }

    private void setBusy(boolean busy) {
        if (loginButton != null) {
            loginButton.setDisable(busy);
        }
        if (registerButton != null) {
            registerButton.setDisable(busy);
        }
        if (continueButton != null) {
            continueButton.setDisable(busy);
        }
    }

    private String describeError(Throwable error) {
        LOG.log(System.Logger.Level.WARNING, "Gọi API xác thực thất bại", error);

        if (error instanceof AuthApiClient.ApiException apiError) {
            String serverMessage = apiError.getServerMessage();
            return switch (apiError.getType()) {
                // Các lỗi mà câu của server cụ thể hơn: sai OTP (còn mấy lần thử), chờ bao lâu, chưa xác minh...
                case BAD_REQUEST -> serverMessage != null ? serverMessage : I18n.get("error.badRequest");
                case FORBIDDEN -> serverMessage != null ? serverMessage : I18n.get("error.forbidden");
                case RATE_LIMITED -> serverMessage != null ? serverMessage : I18n.get("error.unknown");
                case UNAUTHORIZED -> I18n.get("error.login.invalid");
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
        showMessage("");
        if (!validateEmail()) return;

        String email = emailField.getText().trim();
        RegisterData.email = email;
        RegisterData.isResetPasswordFlow = true; // Bật cờ luồng quên mật khẩu

        // Gọi API Server gửi OTP tới email
        runAsync(() -> AUTH_API.forgotPassword(email), response -> {
            showSuccess(response.getMessage());
            openOtpVerification(); // Chuyển sang Màn hình OTP
        });
    }
    @FXML
    private void handleResetPasswordSubmit() {
        showMessage("");
        boolean passOk = validatePassword(true);
        String newPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField != null ? confirmPasswordField.getText() : "";

        if (!passOk) return;
        if (!newPassword.equals(confirmPassword)) {
            showFieldError(confirmPasswordError, confirmPasswordField, "Mật khẩu xác nhận không khớp");
            return;
        }

        String email = RegisterData.email;
        String otp = RegisterData.verifiedOtp;

        // Gọi API gửi Email + OTP + Pass mới lên Server để cập nhật DB
        runAsync(() -> AUTH_API.resetPassword(email, otp, newPassword), response -> {
            RegisterData.reset();
            openLoginView(); // Hoàn tất -> Về màn hình Login
            showSuccess(response.getMessage());
        });
    }
    /** Nút Tiếp tục trên màn hình OTP (khớp onAction="#onContinue" trong FXML). */
    @FXML
    private void onContinue() {
        showMessage("");
        String code = getOtpCode();
        if (code.length() != 6) {
            showMessage(I18n.get("error.otp.incomplete"));
            return;
        }

        String email = RegisterData.email;

        if (RegisterData.isResetPasswordFlow) {
            // Luồng Quên mật khẩu: Lưu OTP lại và chuyển sang Màn hình Reset 2
            RegisterData.verifiedOtp = code;
            switchView("reset-password2-view.fxml", "title.reset");
        } else {
            // Luồng Đăng ký: Xác minh OTP và đăng nhập vào thẳng Trang chủ
            runAsync(() -> AUTH_API.verifyEmail(email, code), this::onLoginSuccess);
        }
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
    private void openOtpVerification() {
        switchView("otp-verification-view.fxml", "title.otpMethod");
    }

    @FXML
    private void openResetPasswordView() {
        switchView("reset-password-view.fxml", "title.reset");
    }

    private void switchView(String fxmlFile, String titleKey) {
        try {
            Stage stage = findStage();
            if (stage == null) {
                throw new IOException("Không tìm thấy cửa sổ hiện tại");
            }
            Navigator.setStage(stage);

            // Mở trang FXML mới
            Navigator.show(VIEW_DIR + fxmlFile, titleKey);
            applyAuthCss(stage.getScene());
            currentFxml = fxmlFile;
            currentTitleKey = titleKey;

            // ===== THÊM ĐOẠN NÀY ĐỂ MỞ RỘNG CỬA SỔ THEO NỘI DUNG MỚI =====
            Platform.runLater(() -> {
                stage.sizeToScene(); // Tự động dãn cửa sổ theo FXML mới
                stage.centerOnScreen(); // Căn giữa màn hình
            });

        } catch (IOException e) {
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
            vn.edu.auction.client.ui.UiTheme.apply(scene);
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
    // 12. MÀN HÌNH NHẬP OTP  [MỚI]
    // ====================================================================
    private void setupOtpView() {
        otpBoxes = new TextField[]{otp1, otp2, otp3, otp4, otp5, otp6};
        for (int i = 0; i < otpBoxes.length; i++) {
            setupOtpBox(i);
        }

        if (emailLabel != null) {
            emailLabel.setText(maskEmail(RegisterData.email));
        }
        startResendCountdown(RESEND_SECONDS); // server vừa gửi mã nên bắt đầu đếm ngược luôn
        Platform.runLater(() -> otpBoxes[0].requestFocus());
    }

    /**
     * Cấu hình 1 ô OTP:
     *  - chỉ nhận 1 chữ số, gõ số mới thì thay số cũ;
     *  - gõ xong tự nhảy sang ô kế tiếp;
     *  - Backspace ở ô trống thì lùi về ô trước;
     *  - dán nguyên "123456" thì tự rải vào 6 ô.
     * VÌ SAO làm vậy: người dùng quen với cách nhập mã này, và dán mã từ email là thao tác rất hay gặp.
     */
    private void setupOtpBox(int index) {
        TextField box = otpBoxes[index];

        UnaryOperator<TextFormatter.Change> filter = change -> {
            String inserted = change.getText();

            if (inserted.isEmpty()) {
                return change; // xóa ký tự
            }
            if (inserted.length() > 1) { // dán nhiều ký tự
                String digits = inserted.replaceAll("\\D", "");
                if (!digits.isEmpty()) {
                    Platform.runLater(() -> fillOtpBoxes(index, digits));
                }
                return null;
            }
            char c = inserted.charAt(0);
            if (c < '0' || c > '9') {
                return null; // không phải chữ số thì bỏ
            }

            // Thay toàn bộ nội dung ô bằng chữ số vừa gõ (ô luôn tối đa 1 ký tự)
            change.setRange(0, change.getControlText().length());
            change.setCaretPosition(1);
            change.setAnchor(1);

            if (!otpFilling && index < otpBoxes.length - 1) {
                Platform.runLater(() -> otpBoxes[index + 1].requestFocus());
            }
            return change;
        };
        box.setTextFormatter(new TextFormatter<>(filter));

        box.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.BACK_SPACE && box.getText().isEmpty() && index > 0) {
                otpBoxes[index - 1].clear();
                otpBoxes[index - 1].requestFocus();
            }
        });
    }

    // Rải chuỗi số vào các ô bắt đầu từ ô start (dùng khi dán mã)
    private void fillOtpBoxes(int start, String digits) {
        otpFilling = true; // báo cho bộ lọc đừng tự nhảy ô trong lúc điền
        try {
            for (int k = 0; k < digits.length() && start + k < otpBoxes.length; k++) {
                otpBoxes[start + k].setText(String.valueOf(digits.charAt(k)));
            }
        } finally {
            otpFilling = false;
        }
        otpBoxes[Math.min(start + digits.length(), otpBoxes.length - 1)].requestFocus();
    }

    private String getOtpCode() {
        StringBuilder code = new StringBuilder();
        for (TextField box : otpBoxes) {
            code.append(box.getText());
        }
        return code.toString();
    }

    private void clearOtpBoxes() {
        for (TextField box : otpBoxes) {
            box.clear();
        }
        otpBoxes[0].requestFocus();
    }



    /** Link "Gửi mã mới". */
    @FXML
    private void onResend() {
        showMessage("");
        String email = RegisterData.email;

        if (RegisterData.isResetPasswordFlow) {
            // Luồng Quên mật khẩu: Gọi lại API forgotPassword để phát lại OTP
            runAsync(() -> AUTH_API.forgotPassword(email), response -> {
                showSuccess(I18n.get("otp.resent"));
                clearOtpBoxes();
                startResendCountdown(RESEND_SECONDS);
            });
        } else {
            // Luồng Đăng ký: Gọi API resendOtp như bình thường
            runAsync(() -> AUTH_API.resendOtp(email), response -> {
                showSuccess(I18n.get("otp.resent"));
                clearOtpBoxes();
                startResendCountdown(RESEND_SECONDS);
            });
        }
    }

    /**
     * Khóa link "Gửi mã mới" và đếm ngược mỗi giây.
     * VÌ SAO: server chỉ cho xin mã mới sau 60 giây; khóa sẵn ở giao diện để người dùng
     * không bấm rồi mới bị báo lỗi.
     */
    private void startResendCountdown(int seconds) {
        if (resendLink == null || countdownLabel == null) {
            return;
        }
        if (resendTimer != null) {
            resendTimer.stop();
        }

        int[] remaining = {seconds};
        resendLink.setDisable(true);
        countdownLabel.setText(String.format(I18n.get("otp.resend.in"), remaining[0]));

        resendTimer = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            remaining[0]--;
            if (remaining[0] <= 0) {
                resendTimer.stop();
                resendLink.setDisable(false);
                countdownLabel.setText("");
            } else {
                countdownLabel.setText(String.format(I18n.get("otp.resend.in"), remaining[0]));
            }
        }));
        resendTimer.setCycleCount(Timeline.INDEFINITE);
        resendTimer.play();
    }

    // an@gmail.com -> an@***... : hiện 3 ký tự đầu + *** + phần @domain
    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 0) {
            return email == null ? "" : email;
        }
        String name = email.substring(0, at);
        return name.substring(0, Math.min(3, name.length())) + "***" + email.substring(at);
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
