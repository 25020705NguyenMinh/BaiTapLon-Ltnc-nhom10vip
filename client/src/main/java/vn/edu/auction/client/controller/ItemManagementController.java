package vn.edu.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;



/**
 * Controller quản lý việc thêm, sửa, xóa và hiển thị danh sách sản phẩm[cite: 1, 3].
 */
public class ItemManagementController {

    @FXML private TableView<ItemMock> itemTable;
    @FXML private TableColumn<ItemMock, Long> idColumn;
    @FXML private TableColumn<ItemMock, String> nameColumn;
    @FXML private TableColumn<ItemMock, String> descriptionColumn;
    @FXML private TableColumn<ItemMock, Double> priceColumn;

    @FXML private TextField nameField;
    @FXML private TextArea descriptionArea;
    @FXML private TextField startingPriceField;
    @FXML private Label messageLabel;


    //ObservableList: list dữ liệu động cho TableView, tbao khi có thay đổi, có thể cập nhật UI ngay lập tức
    private final ObservableList<ItemMock> itemList = FXCollections.observableArrayList();
    private long autoIdCounter = 1;

    @FXML
    public void initialize() {
        // Cấu hình các cột cho TableView
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("startingPrice"));

        itemTable.setItems(itemList);

        // Lắng nghe sự kiện chọn dòng trên bảng để đưa dữ liệu lên Form
        itemTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                nameField.setText(newSelection.getName());
                descriptionArea.setText(newSelection.getDescription());
                startingPriceField.setText(String.valueOf(newSelection.getStartingPrice()));
            }
        });

        loadItems();
    }

    /**
     * Khởi tạo và nạp dữ liệu mẫu cho giao diện thử nghiệm[cite: 3].
     */
    public void loadItems() {
        itemList.clear();
        itemList.add(new ItemMock(autoIdCounter++, "Laptop Gaming", "Core i5, RTX 1650, 16GB RAM", 15000000.0));
        itemList.add(new ItemMock(autoIdCounter++, "Mô hình Gunpla", "BanDai Master Grade 1/100", 1200000.0));
        showMessage("Đã tải danh sách sản phẩm thành công.");
    }

    /**
     * Xử lý thêm sản phẩm mới[cite: 3].
     */
    @FXML
    public void handleCreateItem() {
        if (!validateInput()) return;

        String name = nameField.getText().trim();
        String description = descriptionArea.getText().trim();
        double price = Double.parseDouble(startingPriceField.getText().trim());

        ItemMock newItem = new ItemMock(autoIdCounter++, name, description, price);
        itemList.add(newItem);

        showMessage("Thêm sản phẩm thành công!");
        clearForm();
    }

    /**
     * Xử lý cập nhật thông tin sản phẩm đang được chọn[cite: 3].
     */
    @FXML
    public void handleUpdateItem() {
        ItemMock selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showMessage("Vui lòng chọn một sản phẩm từ bảng để sửa!");
            return;
        }

        if (!validateInput()) return;

        selectedItem.setName(nameField.getText().trim());
        selectedItem.setDescription(descriptionArea.getText().trim());
        selectedItem.setStartingPrice(Double.parseDouble(startingPriceField.getText().trim()));

        itemTable.refresh();
        showMessage("Cập nhật sản phẩm thành công!");
        clearForm();
    }

    /**
     * Xử lý xóa sản phẩm đang chọn[cite: 3].
     */
    @FXML
    public void handleDeleteItem() {
        ItemMock selectedItem = itemTable.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showMessage("Vui lòng chọn một sản phẩm để xóa!");
            return;
        }

        itemList.remove(selectedItem);
        showMessage("Xóa sản phẩm thành công!");
        clearForm();
    }

    /**
     * Xóa các ô nhập liệu trên form[cite: 3].
     */
    @FXML
    public void clearForm() {
        nameField.clear();
        descriptionArea.clear();
        startingPriceField.clear();
        itemTable.getSelectionModel().clearSelection();
    }

    /**
     * Đưa thông báo lên màn hình[cite: 3].
     */
    public void showMessage(String message) {
        if (messageLabel != null) {
            messageLabel.setText(message);
        }
    }

    /**
     * Kiểm tra ràng buộc dữ liệu cơ bản ở giao diện[cite: 3].
     */
    private boolean validateInput() {
        if (nameField.getText().trim().isEmpty()) {
            showMessage("Tên sản phẩm không được để trống!");
            return false;
        }
        try {
            double price = Double.parseDouble(startingPriceField.getText().trim());
            if (price <= 0) {
                showMessage("Giá khởi điểm phải lớn hơn 0!");
                return false;
            }
        } catch (NumberFormatException e) {
            showMessage("Giá khởi điểm phải là một số hợp lệ!");
            return false;
        }
        return true;
    }

    @FXML
    public void handleBackToHome() {
        NavigationController.showHome();
    }
}