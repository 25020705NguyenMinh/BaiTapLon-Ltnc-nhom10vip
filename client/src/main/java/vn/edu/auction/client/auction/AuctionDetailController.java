package vn.edu.auction.client.auction;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.dto.ItemResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class AuctionDetailController {

    @FXML
    private Label itemNameLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label startingPriceLabel;

    @FXML
    private Label currentPriceLabel;

    @FXML
    private Label startTimeLabel;

    @FXML
    private Label endTimeLabel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField bidAmountField;

    private AuctionResponse auction;
    private ItemResponse item;

    private final NumberFormat currencyFormat =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @FXML
    public void initialize() {
        auction = AuctionMockData.getAuction(1L);
        item = AuctionMockData.getItem();

        displayAuctionInformation();
    }

    /**
     * Hiển thị thông tin sản phẩm và phiên đấu giá.
     */
    private void displayAuctionInformation() {
        itemNameLabel.setText(item.getName());
        descriptionLabel.setText(item.getDescription());

        startingPriceLabel.setText(
                formatCurrency(auction.getStartingPrice())
        );

        currentPriceLabel.setText(
                formatCurrency(auction.getCurrentPrice())
        );

        startTimeLabel.setText(
                auction.getStartTime().format(dateFormatter)
        );

        endTimeLabel.setText(
                auction.getEndTime().format(dateFormatter)
        );

        statusLabel.setText(auction.getStatus().toString());
    }

    /**
     * Xử lý thao tác đặt giá.
     */
    @FXML
    private void handlePlaceBid() {
        String input = bidAmountField.getText().trim();

        if (input.isEmpty()) {
            showMessage("Vui lòng nhập giá muốn đặt!");
            return;
        }

        BigDecimal bidAmount;

        try {
            bidAmount = new BigDecimal(input);
        } catch (NumberFormatException e) {
            showMessage("Giá đặt phải là một số hợp lệ!");
            return;
        }

        if (bidAmount.compareTo(BigDecimal.ZERO) <= 0) {
            showMessage("Giá đặt phải lớn hơn 0!");
            return;
        }

        if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
            showMessage("Giá đặt phải cao hơn giá hiện tại!");
            return;
        }

        if (!"RUNNING".equals(auction.getStatus().toString())) {
            showMessage("Phiên đấu giá hiện không còn hoạt động!");
            return;
        }

        // Cập nhật dữ liệu mẫu.
        AuctionMockData.addBid(bidAmount);

        // Cập nhật giao diện.
        currentPriceLabel.setText(formatCurrency(bidAmount));
        bidAmountField.clear();

        showMessage("Đặt giá thành công! Bạn đang dẫn đầu.");
    }

    /**
     * Mở màn hình lịch sử đặt giá.
     */
    @FXML
    private void handleViewBidHistory() {
        navigateTo("/fxml/auction/bid-history-view.fxml");
    }

    /**
     * Mở màn hình kết quả đấu giá.
     */
    @FXML
    private void handleViewResult() {
        navigateTo("/fxml/auction/auction-result-view.fxml");
    }

    /**
     * Chuyển sang một màn hình FXML khác.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Parent root = loader.load();

            Stage stage = (Stage) itemNameLabel
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));

        } catch (IOException | NullPointerException e) {
            showMessage("Không thể mở màn hình: " + fxmlPath);
            e.printStackTrace();
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount) + " VNĐ";
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }
}
