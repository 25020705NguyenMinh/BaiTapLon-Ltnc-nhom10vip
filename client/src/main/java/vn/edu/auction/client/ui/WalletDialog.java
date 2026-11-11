package vn.edu.auction.client.ui;

import java.math.BigDecimal;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Window;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.UserApiClient;
import vn.edu.auction.shared.dto.ExtraDto.WalletTxResponse;

/** Ví: số dư, nạp tiền, rút tiền, lịch sử giao dịch. */
public final class WalletDialog {
    private WalletDialog() {}

    private interface MoneyCall { Object run(BigDecimal v) throws Exception; }

    public static void show(Window owner) {
        UserApiClient api = new UserApiClient();
        Dialog<Void> d = new Dialog<>();
        d.initOwner(owner);
        d.setTitle("Ví của tôi");

        Label balance = new Label("Số dư: ...");
        balance.setStyle("-fx-font-size:18;-fx-font-weight:bold;");
        Label msg = new Label();
        TextField amount = new TextField();
        amount.setPromptText("Số tiền (đ)");
        Button dep = new Button("Nạp tiền");
        Button wd = new Button("Rút tiền");
        TableView<WalletTxResponse> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setPlaceholder(new Label("Chưa có giao dịch."));
        UiKit.col(table, "Thời gian", t -> UiKit.dtSec(t.createdAt()));
        UiKit.col(table, "Loại", t -> "DEPOSIT".equals(t.type()) ? "Nạp tiền" : "Rút tiền");
        UiKit.col(table, "Số tiền", t -> UiKit.money(t.amount()));
        UiKit.col(table, "Số dư sau", t -> UiKit.money(t.balanceAfter()));

        Runnable reload = () -> {
            FxAsync.run(api::wallet, w -> balance.setText("Số dư: " + UiKit.money(w.balance())),
                    e -> msg.setText(BaseApiClient.describe(e)));
            FxAsync.run(api::walletHistory, h -> table.getItems().setAll(h),
                    e -> msg.setText(BaseApiClient.describe(e)));
        };
        dep.setOnAction(e -> submit(amount, msg, api::deposit, reload));
        wd.setOnAction(e -> submit(amount, msg, api::withdraw, reload));

        HBox actions = new HBox(8, amount, dep, wd);
        HBox.setHgrow(amount, Priority.ALWAYS);
        VBox content = new VBox(10, balance, actions, msg, table);
        content.setPrefSize(560, 420);
        d.getDialogPane().setContent(content);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        UiTheme.apply(d.getDialogPane());
        reload.run();
        d.show();
    }

    private static void submit(TextField f, Label msg, MoneyCall call, Runnable reload) {
        BigDecimal v;
        try {
            v = new BigDecimal(f.getText().trim());
        } catch (NumberFormatException e) {
            msg.setText("Số tiền không hợp lệ.");
            return;
        }
        UiKit.act(msg, () -> call.run(v), () -> { f.clear(); reload.run(); });
    }
}
