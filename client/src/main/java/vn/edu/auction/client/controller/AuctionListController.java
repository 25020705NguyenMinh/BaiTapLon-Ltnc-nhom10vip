package vn.edu.auction.client.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.enums.AuctionStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AuctionListController {

    @FXML
    private TableView<AuctionResponse> auctionTable;

    @FXML
    private TableColumn<AuctionResponse, String> itemNameColumn;

    @FXML
    private TableColumn<AuctionResponse, BigDecimal> currentPriceColumn;

    @FXML
    private TableColumn<AuctionResponse, LocalDateTime> endTimeColumn;

    @FXML
    private TableColumn<AuctionResponse, AuctionStatus> statusColumn;

    @FXML
    private Label messageLabel;

    private final ObservableList<AuctionResponse> auctions =
            FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        itemNameColumn.setCellValueFactory(
                new PropertyValueFactory<>("itemName")
        );
        currentPriceColumn.setCellValueFactory(
                new PropertyValueFactory<>("currentPrice")
        );
        endTimeColumn.setCellValueFactory(
                new PropertyValueFactory<>("endTime")
        );
        statusColumn.setCellValueFactory(
                new PropertyValueFactory<>("status")
        );

        auctionTable.getSelectionModel()
                .setSelectionMode(SelectionMode.SINGLE);

        loadAuctions();
    }

    public void loadAuctions() {
        LocalDateTime now = LocalDateTime.now();

        auctions.setAll(
                new AuctionResponse(
                        1L, 1L, "Laptop Dell XPS 13",
                        new BigDecimal("18000000"),
                        new BigDecimal("19500000"),
                        201L,
                        now.minusHours(1),
                        now.plusHours(3),
                        AuctionStatus.RUNNING
                ),
                new AuctionResponse(
                        2L, 2L, "iPhone 15",
                        new BigDecimal("15000000"),
                        new BigDecimal("15000000"),
                        null,
                        now.plusHours(1),
                        now.plusHours(6),
                        AuctionStatus.OPEN
                )
        );

        auctionTable.setItems(auctions);

        if (auctions.isEmpty()) {
            showMessage("Không có phiên đấu giá.");
        } else {
            showMessage("");
        }
    }

    @FXML
    private void handleOpenAuction() {
        AuctionResponse selected =
                auctionTable.getSelectionModel().getSelectedItem();

        if (selected == null) {
            showMessage("Bạn cần chọn một phiên đấu giá.");
            return;
        }

        showMessage(
                "Đã chọn phiên #" + selected.getId()
                        + " - " + selected.getItemName()
        );
    }

    private void showMessage(String message) {
        messageLabel.setText(message);
    }
}