package vn.edu.auction.client.ui;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import vn.edu.auction.shared.dto.BidResponse;
import vn.edu.auction.shared.enums.BidStatus;

public final class BidHistoryChart extends VBox {
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter FULL_TIME = DateTimeFormatter.ofPattern("dd/MM HH:mm:ss");

    public BidHistoryChart(BigDecimal startingPrice, LocalDateTime startTime,
                           List<BidResponse> history, boolean demo) {
        super(10);
        getStyleClass().add("history-panel");
        List<BidResponse> accepted = history.stream()
                .filter(b -> b != null && b.getStatus() == BidStatus.ACCEPTED
                        && b.getBidTime() != null && b.getAmount() != null)
                .sorted(Comparator.comparing(BidResponse::getBidTime)
                        .thenComparing(BidResponse::getBidId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
        Label title = new Label("Lịch sử đặt giá");
        title.getStyleClass().add("history-title");
        Label badge = new Label(demo ? "MINH HỌA" : accepted.size() + " LƯỢT ĐẶT");
        badge.getStyleClass().add("badge");
        Region spacer = new Region();
        HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(new HBox(8, title, spacer, badge));
        if (accepted.isEmpty()) {
            Label empty = new Label("Chưa có lượt đặt giá nào.");
            empty.getStyleClass().add("history-empty");
            getChildren().add(empty);
            return;
        }
        BigDecimal unit = accepted.get(accepted.size() - 1).getAmount()
                .compareTo(new BigDecimal("1000000")) >= 0 ? new BigDecimal("1000000") : new BigDecimal("1000");
        NumberAxis timeAxis = new NumberAxis();
        timeAxis.setForceZeroInRange(true);
        timeAxis.setMinorTickVisible(false);
        timeAxis.setTickLabelFormatter(new StringConverter<>() {
            @Override public String toString(Number value) { return TIME.format(startTime.plusSeconds(value.longValue())); }
            @Override public Number fromString(String value) { throw new UnsupportedOperationException(); }
        });
        NumberAxis priceAxis = new NumberAxis();
        priceAxis.setForceZeroInRange(false);
        priceAxis.setMinorTickVisible(false);
        priceAxis.setLabel(unit.intValue() == 1000000 ? "Giá (triệu đ)" : "Giá (nghìn đ)");
        LineChart<Number, Number> chart = new LineChart<>(timeAxis, priceAxis);
        chart.setAnimated(false);
        chart.setLegendVisible(false);
        chart.setPrefHeight(235);
        chart.setMinHeight(235);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
        chart.getStyleClass().add("bid-history-chart");
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        addPoint(series, startTime, startTime, startingPrice, unit, "Giá khởi điểm");
        for (BidResponse bid : accepted) {
            addPoint(series, startTime, bid.getBidTime(), bid.getAmount(), unit,
                    "Người đặt #" + bid.getBidderId());
        }
        chart.getData().add(series);
        getChildren().add(chart);
        Label hint = new Label(accepted.size() + " lượt đặt giá • Rê chuột vào điểm để xem giá và thời gian");
        hint.getStyleClass().add("detail-note");
        hint.setWrapText(true);
        getChildren().add(hint);
    }

    private void addPoint(XYChart.Series<Number, Number> series, LocalDateTime start,
                          LocalDateTime time, BigDecimal amount, BigDecimal unit, String title) {
        XYChart.Data<Number, Number> point = new XYChart.Data<>(
                Duration.between(start, time).toMillis() / 1000.0,
                amount.divide(unit).doubleValue());
        String money = java.text.NumberFormat.getNumberInstance(java.util.Locale.forLanguageTag("vi-VN")).format(amount);
        String text = title + "\n" + money + " đ\n" + FULL_TIME.format(time);
        point.nodeProperty().addListener((observable, oldNode, node) -> installTooltip(node, text));
        series.getData().add(point);
    }

    private void installTooltip(Node node, String text) {
        if (node != null) Tooltip.install(node, new Tooltip(text));
    }
}
