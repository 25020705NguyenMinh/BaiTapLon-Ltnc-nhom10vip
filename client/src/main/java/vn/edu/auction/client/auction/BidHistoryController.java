package vn.edu.auction.client.auction;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import vn.edu.auction.shared.dto.BidResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class BidHistoryController {

    @FXML
    private VBox bidListContainer;

    @FXML
    private Label totalBidsLabel;

    @FXML
    private Label emptyMessageLabel;

    private final NumberFormat currencyFormat =
            NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"));

    private final DateTimeFormatter dateFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yyyy");

    @FXML
    public void initialize() {
        loadBidHistory();
    }

    /**
     * Lấy lịch sử đặt giá và hiển thị lên giao diện.
     */
    private void loadBidHistory() {
        List<BidResponse> bids = AuctionMockData.getBids(1L);

        bidListContainer.getChildren().clear();
        totalBidsLabel.setText("Tổng số lượt đặt giá: " + bids.size());

        if (bids.isEmpty()) {
            emptyMessageLabel.setVisible(true);
            emptyMessageLabel.setManaged(true);
            return;
        }

        emptyMessageLabel.setVisible(false);
        emptyMessageLabel.setManaged(false);

        // Hiển thị lượt đặt giá mới nhất trước.
        for (int i = bids.size() - 1; i >= 0; i--) {
            BidResponse bid = bids.get(i);
            bidListContainer.getChildren().add(createBidRow(bid));
        }
    }

    /**
     * Tạo một dòng hiển thị thông tin lượt đặt giá.
     */
    private HBox createBidRow(BidResponse bid) {
        HBox row = new HBox(15);
        row.getStyleClass().add("bid-row");

        VBox bidderInfo = new VBox(5);
        bidderInfo.getStyleClass().add("bid-column");

        Label bidderName = new Label(
                AuctionMockData.bidderName(bid.getBidderId())
        );
        bidderName.getStyleClass().add("bidder-name");

        Label bidderId = new Label("ID: " + bid.getBidderId());
        bidderId.getStyleClass().add("bid-secondary");

        bidderInfo.getChildren().addAll(bidderName, bidderId);

        VBox amountInfo = new VBox(5);
        amountInfo.getStyleClass().add("bid-column");

        Label amount = new Label(formatCurrency(bid.getAmount()));
        amount.getStyleClass().add("bid-amount");

        Label status = new Label(bid.getStatus().toString());
        status.getStyleClass().add("bid-secondary");

        amountInfo.getChildren().addAll(amount, status);

        VBox timeInfo = new VBox(5);
        timeInfo.getStyleClass().add("bid-column");

        Label time = new Label(
                bid.getBidTime().format(dateFormatter)
        );
        time.getStyleClass().add("bid-time");

        Label message = new Label(
                bid.getMessage() == null ? "" : bid.getMessage()
        );
        message.getStyleClass().add("bid-secondary");

        timeInfo.getChildren().addAll(time, message);

        row.getChildren().addAll(bidderInfo, amountInfo, timeInfo);

        return row;
    }

    /**
     * Quay lại màn hình chi tiết đấu giá.
     */
    @FXML
    private void handleBack() {
        navigateTo("/fxml/auction/auction-detail-view.fxml");
    }

    /**
     * Chuyển màn hình.
     */
    private void navigateTo(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxmlPath)
            );

            Parent root = loader.load();

            Stage stage = (Stage) bidListContainer
                    .getScene()
                    .getWindow();

            stage.setScene(new Scene(root));

        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    private String formatCurrency(BigDecimal amount) {
        return currencyFormat.format(amount) + " VNĐ";
    }
}
