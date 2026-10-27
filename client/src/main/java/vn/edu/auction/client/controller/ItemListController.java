package vn.edu.auction.client.controller;

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
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        priceColumn.setCellValueFactory(
                new PropertyValueFactory<>("startingPrice")
        );
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

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
                        ItemStatus.DRAFT
                )
        );

        itemTable.setItems(allItems);

        if (allItems.isEmpty()) {
            showMessage("Không có sản phẩm.");
        } else {
            showMessage("");
        }
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