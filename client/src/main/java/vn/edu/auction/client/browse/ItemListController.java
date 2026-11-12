package vn.edu.auction.client.browse;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.ItemApiClient;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.util.Locale;

public class ItemListController {

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
                    case DRAFT -> "Chờ phê duyệt";
                    case SOLD -> "Đã bán";
                    case REMOVED -> "Đã gỡ";
                });
            }
        });
        loadItems();
    }

    private final ItemApiClient itemApi = new ItemApiClient();
    private static Runnable reloadHook = () -> { };

    /** Được BrowseController gọi khi đăng nhập / đăng xuất. */
    public static void reload() { reloadHook.run(); }

    public void loadItems() {
        reloadHook = this::loadItems;
        itemTable.setItems(allItems);
        itemTable.setPlaceholder(new Label("Chưa có sản phẩm nào."));
        if (!SessionData.getInstance().isLoggedIn()) {
            allItems.clear();
            showMessage("Vui lòng đăng nhập để xem danh sách sản phẩm.");
            return;
        }
        showMessage("Đang tải sản phẩm...");
        FxAsync.run(itemApi::getAllItems, items -> {
            allItems.setAll(items);
            showMessage(items.isEmpty() ? "Không có sản phẩm." : "");
        }, err -> showMessage(BaseApiClient.describe(err)));
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
