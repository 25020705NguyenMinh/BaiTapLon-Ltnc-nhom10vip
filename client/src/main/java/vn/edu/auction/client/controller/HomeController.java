package vn.edu.auction.client.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import vn.edu.auction.client.model.SessionData;

/**
 * Controller xử lý màn hình trang chủ[cite: 1, 2].
 */
public class HomeController {

    @FXML private Label welcomeLabel;
    @FXML private Button itemButton;
    @FXML private Button auctionButton;
    @FXML private Button manageItemButton;
    @FXML private Button logoutButton;

    @FXML
    public void initialize() {
        // Lấy thông tin user hiện tại từ SessionData[cite: 2]
        SessionData session = SessionData.getInstance();
        if (session.isLoggedIn()) {
            welcomeLabel.setText("Xin chào, " + session.getFullName() + "!");
        } else {
            welcomeLabel.setText("Xin chào, Khách!");
        }
    }

    @FXML
    public void openItems() {
        // Chuyển sang giao diện danh sách sản phẩm
        NavigationController.showView("/vn/edu/auction/client/fxml/item-list-view.fxml", "Danh Sách Sản Phẩm");
    }

    @FXML
    public void openAuctions() {
        // Chuyển sang giao diện đấu giá[cite: 3]
        NavigationController.showView("/vn/edu/auction/client/fxml/auction-view.fxml", "Sàn Đấu Giá");
    }

    @FXML
    public void openItemManagement() {
        // Chuyển sang giao diện quản lý sản phẩm[cite: 2, 3]
        NavigationController.showView("/vn/edu/auction/client/fxml/item-management-view.fxml", "Quản Lý Sản Phẩm");
    }

    @FXML
    public void handleLogout() {
        // Xóa thông tin phiên đăng nhập và quay về màn hình đăng nhập[cite: 2]
        SessionData.getInstance().clearSession();
        NavigationController.showLogin();
    }
}