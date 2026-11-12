package vn.edu.auction.client.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import vn.edu.auction.client.api.AuctionApiClient;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.ItemApiClient;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.client.ui.UiKit;
import vn.edu.auction.shared.dto.AuctionRequest;
import vn.edu.auction.shared.dto.ItemRequest;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.ItemStatus;

/**
 * Quản lý sản phẩm của người bán (kết nối API thật).
 * Luồng: Thêm mới (chờ admin phê duyệt) -> admin duyệt -> bấm "Tạo phiên đấu giá".
 */
public class ItemManagementController {

    @FXML private TableView<ItemResponse> itemTable;
    @FXML private TableColumn<ItemResponse, Long> idColumn;
    @FXML private TableColumn<ItemResponse, String> nameColumn;
    @FXML private TableColumn<ItemResponse, String> descriptionColumn;
    @FXML private TableColumn<ItemResponse, String> priceColumn;
    @FXML private TableColumn<ItemResponse, String> statusColumn;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField startingPriceField;
    @FXML private Label messageLabel;

    private final ItemApiClient itemApi = new ItemApiClient();
    private final AuctionApiClient auctionApi = new AuctionApiClient();
    private final ObservableList<ItemResponse> itemList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().getId()));
        nameColumn.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().getName()));
        descriptionColumn.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(row.getValue().getDescription()));
        priceColumn.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(UiKit.money(row.getValue().getStartingPrice())));
        statusColumn.setCellValueFactory(row -> new ReadOnlyObjectWrapper<>(statusText(row.getValue().getStatus())));

        itemTable.setItems(itemList);
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                descriptionArea.setText(newSelection.getDescription());
                startingPriceField.setText(newSelection.getStartingPrice().toPlainString());
            }
        });
        loadItems();
    }

    private static String statusText(ItemStatus s) {
        if (s == null) return "—";
        return switch (s) {
            case DRAFT -> "Chờ phê duyệt";
            case ACTIVE -> "Đã duyệt";
            case SOLD -> "Đã bán";
            case REMOVED -> "Đã gỡ";
        };
    }

    /** Chỉ hiện sản phẩm của chính người đang đăng nhập. */
    public void loadItems() {
        Long me = SessionData.getInstance().getUserId();
        showMessage("Đang tải danh sách sản phẩm...");
        FxAsync.run(itemApi::getAllItems, all -> {
            itemList.setAll(all.stream().filter(i -> i.getSellerId().equals(me)).toList());
            showMessage(itemList.isEmpty() ? "Bạn chưa có sản phẩm nào." : "Đã tải danh sách sản phẩm.");
        }, err -> showMessage(BaseApiClient.describe(err)));
    }

    @FXML
    public void handleCreateItem() {
        ItemRequest request = readForm();
        if (request == null) return;
        FxAsync.run(() -> itemApi.createItem(request), created -> {
            showMessage("Đã thêm sản phẩm. Đang chờ quản trị viên phê duyệt.");
            clearForm();
            loadItems();
        }, err -> showMessage(BaseApiClient.describe(err)));
    }

    @FXML
    public void handleUpdateItem() {
        ItemResponse selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Vui lòng chọn một sản phẩm từ bảng để sửa!"); return; }
        ItemRequest request = readForm();
        if (request == null) return;
        FxAsync.run(() -> itemApi.updateItem(selected.getId(), request), updated -> {
            showMessage("Cập nhật sản phẩm thành công!");
            clearForm();
            loadItems();
        }, err -> showMessage(BaseApiClient.describe(err)));
    }

    @FXML
    public void handleDeleteItem() {
        ItemResponse selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Vui lòng chọn một sản phẩm để xóa!"); return; }
        FxAsync.run(() -> { itemApi.deleteItem(selected.getId()); return Boolean.TRUE; }, ok -> {
            showMessage("Xóa sản phẩm thành công!");
            clearForm();
            loadItems();
        }, err -> showMessage(BaseApiClient.describe(err)));
    }

    /** Tạo phiên đấu giá cho sản phẩm ĐÃ ĐƯỢC DUYỆT. Giờ gửi lên là UTC (server dùng đồng hồ UTC). */
    @FXML
    public void handleCreateAuction() {
        ItemResponse selected = itemTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Hãy chọn một sản phẩm để tạo phiên đấu giá."); return; }
        if (selected.getStatus() != ItemStatus.ACTIVE) {
            showMessage("Sản phẩm chưa được quản trị viên phê duyệt (hoặc đã có phiên / đã bán).");
            return;
        }
        TextInputDialog dialog = new TextInputDialog("10");
        dialog.setTitle("Tạo phiên đấu giá");
        dialog.setHeaderText(selected.getName());
        dialog.setContentText("Thời lượng phiên (phút):");
        dialog.showAndWait().ifPresent(text -> {
            long minutes;
            try {
                minutes = Long.parseLong(text.trim());
            } catch (NumberFormatException e) {
                showMessage("Thời lượng phải là số nguyên (phút).");
                return;
            }
            if (minutes <= 0 || minutes > 60 * 24 * 30) { showMessage("Thời lượng phải từ 1 phút đến 30 ngày."); return; }
            LocalDateTime start = LocalDateTime.now(ZoneOffset.UTC);
            AuctionRequest request = new AuctionRequest(selected.getId(), start, start.plusMinutes(minutes));
            FxAsync.run(() -> auctionApi.createAuction(request), created -> {
                showMessage("Đã tạo phiên đấu giá #" + created.getId() + ". Người theo dõi bạn sẽ nhận thông báo.");
                loadItems();
            }, err -> showMessage(BaseApiClient.describe(err)));
        });
    }

    @FXML
    public void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        startingPriceField.clear();
        itemTable.getSelectionModel().clearSelection();
    }

    public void showMessage(String message) {
        if (messageLabel != null) messageLabel.setText(message);
    }

    /** Kiểm tra cơ bản ở giao diện; server vẫn kiểm tra lại. */
    private ItemRequest readForm() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) { showMessage("Tên sản phẩm không được để trống!"); return null; }
        BigDecimal price;
        try {
            price = new BigDecimal(startingPriceField.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Giá khởi điểm phải là một số hợp lệ!");
            return null;
        }
        if (price.signum() <= 0) { showMessage("Giá khởi điểm phải lớn hơn 0!"); return null; }
        return new ItemRequest(name, descriptionArea.getText().trim(), price);
    }

    @FXML
    public void handleBackToHome() {
        NavigationController.showHome();
    }
}
