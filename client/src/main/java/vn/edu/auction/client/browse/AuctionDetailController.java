package vn.edu.auction.client.browse;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.dto.*;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.client.api.*;


public class AuctionDetailController {
    @FXML private Label itemNameLabel, currentPriceLabel, highestBidderLabel, statusLabel, endTimeLabel, messageLabel;
    @FXML private TextField bidAmountField;
    @FXML private Button placeBidButton;
    @FXML private TableView<BidResponse> bidTable;

    private final AuctionApiClient auctionApi = new AuctionApiClient();
    private final BidApiClient bidApi = new BidApiClient();
    private Long auctionId;
    private Runnable unsubscribe;

    public void setAuctionId(Long id) {
        this.auctionId = id;
        loadAuction();
        loadBidHistory();
        unsubscribe = AuctionRealtimeClient.getInstance().subscribe(this::onEvent);
    }

    public void loadAuction() {
        FxAsync.run(() -> auctionApi.getAuctionById(auctionId), this::render,
                err -> showMessage(BaseApiClient.describe(err)));
    }

    public void loadBidHistory() { // dùng chung với BidHistoryController
        FxAsync.run(() -> bidApi.getBidHistory(auctionId),
                list -> bidTable.getItems().setAll(list),
                err -> showMessage(BaseApiClient.describe(err)));
    }

    @FXML
    private void handlePlaceBid() {
        BigDecimal amount;
        try {
            amount = new BigDecimal(bidAmountField.getText().trim());
        } catch (NumberFormatException e) {
            showMessage("Số tiền không hợp lệ.");
            return;
        }
        placeBidButton.setDisable(true);
        FxAsync.run(() -> bidApi.placeBid(auctionId, new PlaceBidRequest(amount)),
                res -> { placeBidButton.setDisable(false); bidAmountField.clear(); showMessage(res.getMessage()); },
                err -> { placeBidButton.setDisable(false); showMessage(BaseApiClient.describe(err)); });
    }

    private void onEvent(AuctionRealtimeClient.Event ev) {
        if (auctionId == null || !auctionId.equals(ev.auctionId())) return;
        currentPriceLabel.setText(NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN"))
                .format(ev.currentPrice()) + " đ");
        highestBidderLabel.setText(ev.highestBidderId() == null ? "—" : "Người dùng #" + ev.highestBidderId());
        statusLabel.setText(String.valueOf(ev.status()));
        endTimeLabel.setText(String.valueOf(ev.endTime()));

        if ("NEW_BID".equals(ev.type())) {
            loadBidHistory();                       // cập nhật lịch sử
        } else if ("AUCTION_FINISHED".equals(ev.type())) {
            placeBidButton.setDisable(true);
            bidAmountField.setDisable(true);
            showMessage(ev.highestBidderId() == null
                    ? "Phiên đã kết thúc, không có người thắng."
                    : "Phiên đã kết thúc. Người thắng: #" + ev.highestBidderId());
        }
    }

    private void render(AuctionResponse a) { /* set text cho các label từ a */ }
    private void showMessage(String m) { messageLabel.setText(m); }
}