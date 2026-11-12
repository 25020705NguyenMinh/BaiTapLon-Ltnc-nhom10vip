package vn.edu.auction.client.auction;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.dto.ItemResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Controller xử lý màn hình kết quả đấu giá.
 */
public class AuctionResultController {

    @FXML
    private Label itemNameLabel;

    @FXML
    private Label auctionStatusLabel;

    @FXML
    private Label winnerLabel;

    @FXML
    private Label finalPriceLabel;

    @FXML
    private Label endTimeLabel;

    private final NumberFormat currencyFormat =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @FXML
    public void initialize() {
        displayAuctionResult();
    }

    /**
     * Lấy dữ liệu và hiển thị kết quả phiên đấu giá.
     */
    private void displayAuctionResult() {
        AuctionResponse auction = AuctionMockData.getAuction(1L);
        ItemResponse item = AuctionMockData.getItem();

        itemNameLabel.setText(item.getName());
        auctionStatusLabel.setText(auction.getStatus().toString());

        // Hiển thị tên người thắng (lấy từ dữ liệu mock đã cập nhật)
        String winner = AuctionMockData.currentWinnerName();
        winnerLabel.setText(winner);

        // Hiển thị giá cuối cùng / giá hiện tại
        finalPriceLabel.setText(formatCurrency(auction.getCurrentPrice()));

        // Thời gian kết thúc
        if (auction.getEndTime() != null) {
            endTimeLabel.setText(auction.getEndTime().format(dateFormatter));
        } else {
            endTimeLabel.setText("N/A");
        }
    }

    /**
     * Quay lại màn hình chi tiết đấu giá.
     */
    @FXML
    private void handleBackToDetail() {
        navigateTo("/fxml/auction/auction-detail-view.fxml");
    }

    /**
     * Chuyển sang màn hình FXML khác.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );
            Parent root = loader.load();

            Stage stage = (Stage) itemNameLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0 VNĐ";
        return currencyFormat.format(amount) + " VNĐ";
    }
}