package vn.edu.auction.client.browse;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import vn.edu.auction.client.api.AuctionApiClient;
import vn.edu.auction.client.api.AuctionRealtimeClient;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.BidApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.client.ui.BidHistoryChart;
import vn.edu.auction.client.ui.UiKit;
import vn.edu.auction.client.ui.WatchBar;
import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.dto.BidResponse;
import vn.edu.auction.shared.dto.PlaceBidRequest;
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
    private final AuctionApiClient auctionApi = new AuctionApiClient();
    private final BidApiClient bidApi = new BidApiClient();
    private static Runnable reloadHook = () -> { };
    private Runnable unsubscribe;

    /** Được BrowseController gọi khi đăng nhập / đăng xuất. */
    public static void reload() { reloadHook.run(); }

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
                super.updateItem(value, empty); setText(empty || value == null ? null : UiKit.dt(value));
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
        // Realtime: có bid mới / phiên kết thúc thì cập nhật đúng dòng, không cần tải lại
        unsubscribe = AuctionRealtimeClient.getInstance().subscribe(this::onAuctionEvent);
        auctionTable.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && unsubscribe != null) { unsubscribe.run(); unsubscribe = null; }
        });
        loadAuctions();
    }

    public void loadAuctions() {
        reloadHook = this::loadAuctions;
        auctionTable.setItems(auctions);
        auctionTable.setPlaceholder(new Label("Chưa có phiên đấu giá nào."));
        if (!SessionData.getInstance().isLoggedIn()) {
            auctions.clear();
            showMessage("Vui lòng đăng nhập để xem các phiên đấu giá.");
            return;
        }
        showMessage("Đang tải phiên đấu giá...");
        FxAsync.run(auctionApi::getAllAuctions, list -> {
            auctions.setAll(list);
            showMessage(list.isEmpty() ? "Không có phiên đấu giá." : "");
        }, err -> showMessage(BaseApiClient.describe(err)));
    }

    private void onAuctionEvent(AuctionRealtimeClient.Event ev) {
        for (AuctionResponse a : auctions) {
            if (a.getId().equals(ev.auctionId())) {
                a.setCurrentPrice(ev.currentPrice());
                a.setHighestBidderId(ev.highestBidderId());
                a.setStatus(ev.status());
                a.setEndTime(ev.endTime());
                auctionTable.refresh();
                return;
            }
        }
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

        Label id = new Label();
        id.getStyleClass().add("badge");
        Label caption = new Label("GIÁ HIỆN TẠI");
        caption.getStyleClass().add("detail-caption");
        Label price = new Label();
        price.getStyleClass().add("detail-price");
        VBox priceBlock = new VBox(4, caption, price);
        Label starting = new Label("Giá khởi điểm    " + money(selected.getStartingPrice()));
        Label leader = new Label();
        Label end = new Label();
        VBox metadata = new VBox(10, starting, leader, end);
        metadata.getStyleClass().add("detail-meta");
        Label note = new Label();
        note.getStyleClass().add("detail-note");
        note.setWrapText(true);
        VBox historyHolder = new VBox();

        // Ô đặt giá
        TextField amountField = new TextField();
        amountField.setPromptText("Nhập giá bạn muốn đặt (đ)");
        HBox.setHgrow(amountField, Priority.ALWAYS);
        Button bidButton = new Button("Đặt giá");
        bidButton.getStyleClass().add("primary-button");
        HBox bidRow = new HBox(8, amountField, bidButton);
        bidRow.setAlignment(Pos.CENTER_LEFT);

        // Hiển thị trạng thái hiện tại của phiên (dùng chung cho dữ liệu ban đầu và event realtime)
        java.util.function.Consumer<AuctionResponse> render = a -> {
            id.setText("PHIÊN #" + a.getId() + "   •   " + status(a.getStatus()));
            price.setText(money(a.getCurrentPrice()));
            leader.setText("Người dẫn đầu    " + (a.getHighestBidderId() == null ? "—" : "#" + a.getHighestBidderId()));
            end.setText("Kết thúc              " + UiKit.dt(a.getEndTime()));
            boolean active = a.getStatus() == AuctionStatus.RUNNING || a.getStatus() == AuctionStatus.OPEN;
            bidRow.setDisable(!active || !SessionData.getInstance().isLoggedIn());
            if (!active) note.setText(a.getStatus() == AuctionStatus.CANCELED
                    ? "Phiên đã bị quản trị viên dừng." : "Phiên đã kết thúc.");
        };
        render.accept(selected);

        Runnable loadHistory = () -> FxAsync.run(() -> bidApi.getBidHistory(selected.getId()), (List<BidResponse> list) ->
                historyHolder.getChildren().setAll(new BidHistoryChart(selected.getStartingPrice(), selected.getStartTime(), list, false)),
                err -> note.setText(BaseApiClient.describe(err)));
        if (SessionData.getInstance().isLoggedIn()) loadHistory.run();

        bidButton.setOnAction(e -> {
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountField.getText().trim());
            } catch (NumberFormatException ex) {
                note.setText("Số tiền không hợp lệ.");
                return;
            }
            bidButton.setDisable(true);
            FxAsync.run(() -> bidApi.placeBid(selected.getId(), new PlaceBidRequest(amount)),
                    res -> { bidButton.setDisable(false); amountField.clear(); note.setText(res.getMessage()); loadHistory.run(); },
                    err -> { bidButton.setDisable(false); note.setText(BaseApiClient.describe(err)); });
        });

        // Realtime trong dialog: giá, người dẫn đầu, giờ kết thúc (gia hạn chống sniping), lịch sử bid
        Runnable unsub = AuctionRealtimeClient.getInstance().subscribe(ev -> {
            if (!selected.getId().equals(ev.auctionId())) return;
            selected.setCurrentPrice(ev.currentPrice());
            selected.setHighestBidderId(ev.highestBidderId());
            selected.setStatus(ev.status());
            selected.setEndTime(ev.endTime());
            render.accept(selected);
            auctionTable.refresh();
            loadHistory.run();
        });
        detail.setOnHidden(e -> unsub.run());

        VBox content = new VBox(16, id, new WatchBar(selected.getId(), selected.getItemId()),
                priceBlock, metadata, bidRow, historyHolder, note);
        content.getStyleClass().add("detail-content");
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefViewportHeight(540);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.getStyleClass().add("detail-scroll");
        detail.getDialogPane().setContent(scroll);
        detail.setResizable(true);
        vn.edu.auction.client.ui.UiTheme.apply(detail.getDialogPane());
        ButtonType close = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        detail.getDialogPane().getButtonTypes().add(close);
        if (!SessionData.getInstance().isLoggedIn()) {
            note.setText("Đăng nhập để đặt giá.");
        }
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
