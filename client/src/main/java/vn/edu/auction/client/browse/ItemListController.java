package vn.edu.auction.client.browse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.ItemStatus;
import vn.edu.auction.client.api.*;

import java.math.BigDecimal;
import java.util.Locale;


public class ItemListController {


    private final ItemApiClient itemApi = new ItemApiClient();
    @FXML
    private TableView<ItemResponse> itemTable;

    @FXML
    private TableColumn<ItemResponse, String> nameColumn;

    @FXML
    private TableColumn<ItemResponse, BigDecimal> priceColumn;

    @FXML
    private TableColumn<ItemResponse, ItemStatus> statusColumn;

    @FXML
    private TextField searchField;

    @FXML
    private Label messageLabel;

    private final ObservableList<ItemResponse> allItems =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        itemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("startingPrice")
        );
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        priceColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : java.text.NumberFormat.getNumberInstance(
                        Locale.forLanguageTag("vi-VN")).format(value) + " đ");
            }
        });
        statusColumn.setCellFactory(column -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(ItemStatus value, boolean empty) {
                super.updateItem(value, empty);
                setText(empty || value == null ? null : switch (value) {
                    case ACTIVE -> "Đang niêm yết";
                    case DRAFT -> "Bản nháp";
                    case SOLD -> "Đã bán";
                    case REMOVED -> "Đã gỡ";
                });
            }
        });
        loadItems();
    }

    public void loadItems() {
        allItems.setAll(
                new ItemResponse(
                        1L,
                        101L,
                        "Laptop Dell XPS 13",
                        "Laptop văn phòng",
                        new BigDecimal("18000000"),
                        ItemStatus.ACTIVE
                ),
                new ItemResponse(
                        2L,
                        102L,
                        "iPhone 15",
                        "Điện thoại đã qua sử dụng",
                        new BigDecimal("15000000"),
                        ItemStatus.ACTIVE
                ),
                new ItemResponse(
                        3L,
                        103L,
                        "Máy ảnh Sony",
                        "Máy ảnh mirrorless",
                        new BigDecimal("12000000"),
                        ItemStatus.ACTIVE
                )
        );

        itemTable.setItems(allItems);

        if (allItems.isEmpty()) {
            showMessage("Không có sản phẩm.");
        } else {
            showMessage("");
        }
            showMessage("Đang tải sản phẩm...");
            FxAsync.run(itemApi::getAllItems,
                    items -> {
                        allItems.setAll(items);
                        itemTable.setItems(allItems);
                        showMessage(items.isEmpty() ? "Không có sản phẩm." : "");
                    },
                    err -> showMessage(BaseApiClient.describe(err)));
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (keyword.isEmpty()) {
            itemTable.setItems(allItems);
            showMessage("");
            return;
        }

        ObservableList<ItemResponse> result = allItems.filtered(item ->
                item.getName() != null
                        && item.getName()
                        .toLowerCase(Locale.ROOT)
                        .contains(keyword)
        );

        itemTable.setItems(result);

        if (result.isEmpty()) {
            showMessage("Không tìm thấy sản phẩm phù hợp.");
        } else {
            showMessage("Tìm thấy " + result.size() + " sản phẩm.");
        }
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }
}
