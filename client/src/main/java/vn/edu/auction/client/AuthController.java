package vn.edu.auction.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;package vn.edu.auction.client;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.getItems().setAll("BUYER", "SELLER");
            roleComboBox.setValue("BUYER");
        }

        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showMessage("Vui lòng nhập email!");
            return;
        }

        if (password.isEmpty()) {
            showMessage("Vui lòng nhập mật khẩu!");
            return;
        }

        showMessage("Giao diện đã sẵn sàng. Kết nối API sẽ được bổ sung ở giai đoạn 3.");
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty()) {
            showMessage("Vui lòng nhập họ và tên!");
            return;
        }

        if (email.isEmpty()) {
            showMessage("Vui lòng nhập email!");
            return;
        }

        if (password.isEmpty()) {
            showMessage("Vui lòng nhập mật khẩu!");
            return;
        }

        if (roleComboBox == null || roleComboBox.getValue() == null) {
            showMessage("Vui lòng chọn vai trò!");
            return;
        }

        showMessage("Giao diện đã sẵn sàng. Kết nối API sẽ được bổ sung ở giai đoạn 3.");
    }


    @FXML
    private void handleConfirmReset(){
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            showMessage("Vui lòng nhập email!");
            return;
        }
        showMessage("Giao diện đã sẵn sàng. Kết nối API sẽ được bổ sung ở giai đoạn 3.");
    }
    @FXML
    private void openRegisterView() {
        switchView("register-view.fxml", "Đăng ký tài khoản");
    }

    @FXML
    private void openLoginView() {
        switchView("login-view.fxml", "Đăng nhập");
    }

    @FXML
    private void openResetPasswordView(){switchView("reset-password-view.fxml","Quên mật khẩu?");}

    @FXML
    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    private void switchView(String fxmlFile, String title) {
        try {
            var resource = getClass().getResource("/vn/edu/auction/client/" + fxmlFile);

            if (resource == null) {
                showMessage("Không tìm thấy giao diện: " + fxmlFile);
                return;
            }

           Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        var css = getClass().getResource("/vn/edu/auction/client/auth.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();

        } catch (IOException e) {
            showMessage("Không thể mở giao diện: " + e.getMessage());
        }
    }
}


import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class AuthController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Label messageLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button registerButton;

    @FXML
    public void initialize() {
        if (roleComboBox != null) {
            roleComboBox.getItems().setAll("BUYER", "SELLER");
            roleComboBox.setValue("BUYER");
        }

        if (messageLabel != null) {
            messageLabel.setText("");
        }
    }

    @FXML
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty()) {
            showMessage("Vui lòng nhập email!");
            return;
        }

        if (password.isEmpty()) {
            showMessage("Vui lòng nhập mật khẩu!");
            return;
        }

        showMessage("Giao diện đã sẵn sàng. Kết nối API sẽ được bổ sung ở giai đoạn 3.");
    }

    @FXML
    private void handleRegister() {
        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (fullName.isEmpty()) {
            showMessage("Vui lòng nhập họ và tên!");
            return;
        }

        if (email.isEmpty()) {
            showMessage("Vui lòng nhập email!");
            return;
        }

        if (password.isEmpty()) {
            showMessage("Vui lòng nhập mật khẩu!");
            return;
        }

        if (roleComboBox == null || roleComboBox.getValue() == null) {
            showMessage("Vui lòng chọn vai trò!");
            return;
        }

        showMessage("Giao diện đã sẵn sàng. Kết nối API sẽ được bổ sung ở giai đoạn 3.");
    }

    @FXML
    private void openRegisterView() {
        switchView("register-view.fxml", "Đăng ký tài khoản");
    }

    @FXML
    private void openLoginView() {
        switchView("login-view.fxml", "Đăng nhập");
    }

    @FXML
    private void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    private void switchView(String fxmlFile, String title) {
        try {
            var resource = getClass().getResource("/vn/edu/auction/client/" + fxmlFile);

            if (resource == null) {
                showMessage("Không tìm thấy giao diện: " + fxmlFile);
                return;
            }

           Parent root = FXMLLoader.load(resource);
        Scene scene = new Scene(root);
        var css = getClass().getResource("/vn/edu/auction/client/auth.css");

        if (css != null) {
            scene.getStylesheets().add(css.toExternalForm());
        }

        Stage stage = (Stage) messageLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle(title);
        stage.centerOnScreen();

        } catch (IOException e) {
            showMessage("Không thể mở giao diện: " + e.getMessage());
        }
    }
}

