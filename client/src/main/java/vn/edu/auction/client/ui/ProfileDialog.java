package vn.edu.auction.client.ui;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.UserApiClient;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.dto.ExtraDto.ChangePasswordRequest;
import vn.edu.auction.shared.dto.ExtraDto.UpdateProfileRequest;

/** Thông tin tài khoản: tên, email, địa chỉ, số điện thoại, đổi mật khẩu. */
public final class ProfileDialog {
    private ProfileDialog() {}

    public static void show(Window owner, Runnable onSaved) {
        UserApiClient api = new UserApiClient();
        Dialog<Void> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle("Thông tin tài khoản");

        TextField name = new TextField();
        TextField email = new TextField();
        TextField phone = new TextField();
        TextField address = new TextField();
        email.setDisable(true);
        PasswordField oldPw = new PasswordField();
        PasswordField newPw = new PasswordField();
        Label msg = new Label();
        msg.setWrapText(true);
        Button save = new Button("Lưu thông tin");
        Button change = new Button("Đổi mật khẩu");

        GridPane g = new GridPane();
        g.setHgap(10);
        g.setVgap(8);
        g.setPadding(new Insets(10));
        g.addRow(0, new Label("Họ tên"), name);
        g.addRow(1, new Label("Email"), email);
        g.addRow(2, new Label("Địa chỉ"), address);
        g.addRow(3, new Label("Số điện thoại"), phone);
        g.add(save, 1, 4);
        g.add(new Separator(), 0, 5, 2, 1);
        g.addRow(6, new Label("Mật khẩu hiện tại"), oldPw);
        g.addRow(7, new Label("Mật khẩu mới"), newPw);
        g.add(change, 1, 8);
        g.add(msg, 0, 9, 2, 1);
        for (TextField f : List.of(name, email, phone, address, oldPw, newPw)) f.setPrefWidth(280);

        FxAsync.run(api::profile, p -> {
            name.setText(p.fullName());
            email.setText(p.email());
            phone.setText(p.phone() == null ? "" : p.phone());
            address.setText(p.address() == null ? "" : p.address());
        }, e -> msg.setText(BaseApiClient.describe(e)));

        save.setOnAction(e -> FxAsync.run(
                () -> api.updateProfile(new UpdateProfileRequest(name.getText(), phone.getText(), address.getText())),
                p -> {
                    SessionData.getInstance().setFullName(p.fullName());
                    msg.setText("Đã lưu thông tin.");
                    onSaved.run();
                },
                err -> msg.setText(BaseApiClient.describe(err))));

        change.setOnAction(e -> FxAsync.run(
                () -> { api.changePassword(new ChangePasswordRequest(oldPw.getText(), newPw.getText())); return Boolean.TRUE; },
                ok -> { oldPw.clear(); newPw.clear(); msg.setText("Đã đổi mật khẩu."); },
                err -> msg.setText(BaseApiClient.describe(err))));

        d.getDialogPane().setContent(g);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        UiTheme.apply(d.getDialogPane());
        d.show();
    }
}
