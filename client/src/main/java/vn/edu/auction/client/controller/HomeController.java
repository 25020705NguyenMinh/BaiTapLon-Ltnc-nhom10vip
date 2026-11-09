package vn.edu.auction.client.controller;

import javafx.fxml.FXML;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.client.navigation.Navigator;

public class HomeController {

    @FXML
    private void openItems() {
        handleBrowseAction();
    }
    @FXML
    private void openAuctions() {
        handleBrowseAction();
    }
    // Nút Quản lý sản phẩm
    @FXML
    private void openItemManagement() {
        handleSellAction();
    }
    // Nút Bán hàng / Đăng sản phẩm
    @FXML
    private void handleSellAction() {
        if (!isLoggedIn()) {
            // Chưa đăng nhập -> Nhảy sang màn hình Đăng nhập
            Navigator.navigateTo(Navigator.LOGIN_VIEW);
        } else {
            // Đã đăng nhập -> Cho phép vào màn hình Quản lý/Đăng sản phẩm
            Navigator.navigateTo("/vn/edu/auction/client/fxml/home/item-management-view.fxml");
        }
    }

    // Nút Xem/Tham gia Đấu giá
    @FXML
    private void handleBrowseAction() {
        // Có thể cho xem danh sách trước, hoặc bắt đăng nhập tùy logic bài tập
        if (!isLoggedIn()) {
            Navigator.navigateTo(Navigator.LOGIN_VIEW);
        } else {
            Navigator.navigateTo(Navigator.BROWSE_VIEW);
        }
    }

    // Nút Đăng nhập trên thanh Header
    @FXML
    private void handleLoginButton() {
        Navigator.navigateTo(Navigator.LOGIN_VIEW);
    }

    @FXML
    private void handleLogout() {
        // Xóa thông tin người dùng đang đăng nhập
        SessionData.getInstance().clearSession();

        // Quay về màn hình đăng nhập
        NavigationController.showLogin();
    }
    // Hàm tiện ích kiểm tra xem đã đăng nhập hay chưa
    private boolean isLoggedIn() {
        return SessionData.getInstance().isLoggedIn();
    }
}
