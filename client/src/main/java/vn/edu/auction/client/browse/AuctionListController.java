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
import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.enums.AuctionStatus;

public class AuctionListController {
    @FXML private TableView<AuctionResponse> auctionTable;
    @FXML private TableColumn<AuctionResponse, String> itemNameColumn;
    @FXML private TableColumn<AuctionResponse, BigDecimal> currentPriceColumn;
    @FXML private TableColumn<AuctionResponse, LocalDateTime> endTimeColumn;
    @FXML private TableColumn<AuctionResponse, AuctionStatus> statusColumn;
    @FXML private TextField searchField;
    @FXML private Label messageLabel;
    private final ObservableList<AuctionResponse> auctions = FXCollections.observableArrayList();
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    @FXML private void initialize() {
        auctionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        itemNameColumn.setCellValueFactory(new PropertyValueFactory<>("itemName"));
        currentPriceColumn.setCellValueFactory(new PropertyValueFactory<>("currentPrice"));
        endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        currentPriceColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(BigDecimal value, boolean empty) {
                super.updateItem(value, empty); setText(empty || value == null ? null : money(value));
            }
        });
        endTimeColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime value, boolean empty) {
                super.updateItem(value, empty); setText(empty || value == null ? null : DATE.format(value));
            }
        });
        statusColumn.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(AuctionStatus value, boolean empty) {
                super.updateItem(value, empty); setText(empty || value == null ? null : status(value));
            }
        });
        auctionTable.setRowFactory(view -> {
            TableRow<AuctionResponse> row = new TableRow<>();
            row.setOnMouseClicked(event -> { if (event.getClickCount() == 2 && !row.isEmpty()) handleOpenAuction(); });
            return row;
        });
        loadAuctions();
    }
    public void loadAuctions() {
        LocalDateTime now = LocalDateTime.now();
        auctions.setAll(
            new AuctionResponse(1L, 1L, "Laptop Dell XPS 13", new BigDecimal("18000000"), new BigDecimal("19500000"), 201L, now.minusHours(1), now.plusHours(3), AuctionStatus.RUNNING),
            new AuctionResponse(2L, 2L, "iPhone 15", new BigDecimal("15000000"), new BigDecimal("15000000"), null, now.plusHours(1), now.plusHours(6), AuctionStatus.OPEN),
            new AuctionResponse(3L, 3L, "Máy ảnh Sony", new BigDecimal("12000000"), new BigDecimal("13200000"), 203L, now.minusHours(2), now.plusDays(1), AuctionStatus.RUNNING)
        );
        auctionTable.setItems(auctions);
        auctionTable.setPlaceholder(new Label("Chưa có phiên đấu giá nào."));
        showMessage(auctions.isEmpty() ? "Không có phiên đấu giá." : "");
    }
    @FXML private void handleSearch() {
        String keyword = searchField.getText().trim().toLowerCase(Locale.ROOT);
        ObservableList<AuctionResponse> result = auctions.filtered(a -> a.getItemName() != null && a.getItemName().toLowerCase(Locale.ROOT).contains(keyword));
        auctionTable.setItems(result);
        showMessage(result.isEmpty() ? "Không tìm thấy phiên phù hợp." : "");
    }
    @FXML private void handleOpenAuction() {
        AuctionResponse selected = auctionTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showMessage("Hãy chọn một phiên đấu giá trước."); return; }
        showMessage("");
        Dialog<Void> detail = new Dialog<>();
        detail.initOwner(auctionTable.getScene().getWindow());
        detail.setTitle("Chi tiết phiên đấu giá");
        detail.setHeaderText(selected.getItemName());
        Label id = new Label("PHIÊN #" + selected.getId() + "   •   " + status(selected.getStatus()));
        id.getStyleClass().add("badge");
        Label caption = new Label("GIÁ HIỆN TẠI");
        caption.getStyleClass().add("detail-caption");
        Label price = new Label(money(selected.getCurrentPrice()));
        price.getStyleClass().add("detail-price");
        VBox priceBlock = new VBox(4, caption, price);
        Label starting = new Label("Giá khởi điểm    " + money(selected.getStartingPrice()));
        Label end = new Label("Kết thúc              " + DATE.format(selected.getEndTime()));
        VBox metadata = new VBox(10, starting, end);
        metadata.getStyleClass().add("detail-meta");
        Label note = new Label("Dữ liệu minh họa. Chức năng đặt giá đang được hoàn thiện.");
        note.getStyleClass().add("detail-note");
        note.setWrapText(true);
        vn.edu.auction.client.ui.BidHistoryChart historyChart = new vn.edu.auction.client.ui.BidHistoryChart(
                selected.getStartingPrice(), selected.getStartTime(), DemoBidHistory.forAuction(selected), true);
        VBox content = new VBox(16, id, priceBlock, metadata, historyChart, note);
        content.getStyleClass().add("detail-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(540);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("detail-scroll");
        detail.getDialogPane().setContent(scroll);
        detail.setResizable(true);
        vn.edu.auction.client.ui.UiTheme.apply(detail.getDialogPane());
        ButtonType participate = new ButtonType("Tham gia đấu giá", ButtonBar.ButtonData.OTHER);
        ButtonType close = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        detail.getDialogPane().getButtonTypes().addAll(participate, close);
        Button button = (Button) detail.getDialogPane().lookupButton(participate);
        button.getStyleClass().add("primary-button");
        button.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            e.consume();
            if (!SessionData.getInstance().isLoggedIn()) {
                BrowseController.openLogin((Stage) detail.getDialogPane().getScene().getWindow(), BrowseController::refreshCurrentAccount);
            } else {
                note.setText("Bạn đã đăng nhập. Chức năng đặt giá thật sẽ được bổ sung ở giai đoạn sau.");
            }
        });
        detail.show();
    }
    private static String money(BigDecimal value) { return value == null ? "—" : NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(value) + " đ"; }
    private static String status(AuctionStatus value) {
        if (value == null) return "—";
        return switch (value) {
            case RUNNING -> "Đang diễn ra";
            case OPEN -> "Sắp diễn ra";
            case FINISHED -> "Đã kết thúc";
            case PAID -> "Đã thanh toán";
            case CANCELED -> "Đã hủy";
        };
    }
    private void showMessage(String message) { messageLabel.setText(message); }
}
