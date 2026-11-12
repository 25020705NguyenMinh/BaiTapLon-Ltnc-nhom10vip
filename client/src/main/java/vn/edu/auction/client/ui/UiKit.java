package vn.edu.auction.client.ui;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.function.Function;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;

/** Tiện ích giao diện dùng chung. */
public final class UiKit {
    private UiKit() {}

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DT_SEC = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static String money(BigDecimal v) {
        return v == null ? "—" : NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(v) + " đ";
    }

    /** Server lưu giờ UTC => đổi sang giờ máy người dùng trước khi hiển thị. */
    public static LocalDateTime toLocal(LocalDateTime utc) {
        return utc == null ? null : utc.atOffset(ZoneOffset.UTC).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String dt(LocalDateTime utc) { return utc == null ? "—" : DT.format(toLocal(utc)); }
    public static String dtSec(LocalDateTime utc) { return utc == null ? "—" : DT_SEC.format(toLocal(utc)); }

    /** Thêm 1 cột chữ vào bảng. Truyền table vào để Java tự suy ra kiểu T. */
    public static <T> void col(TableView<T> table, String title, Function<T, Object> value) {
        TableColumn<T, String> c = new TableColumn<>(title);
        c.setCellValueFactory(cd -> {
            Object v = value.apply(cd.getValue());
            return new SimpleStringProperty(v == null ? "—" : v.toString());
        });
        table.getColumns().add(c);
    }

    /** Gọi API ở nền; thành công thì chạy reload, lỗi thì hiện câu dễ hiểu lên label. */
    public static void act(Label msg, Callable<?> call, Runnable reload) {
        FxAsync.run(call, r -> { msg.setText(""); reload.run(); }, e -> msg.setText(BaseApiClient.describe(e)));
    }
}
