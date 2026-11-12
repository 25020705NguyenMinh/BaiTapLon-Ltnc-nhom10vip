package vn.edu.auction.client.api;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.enums.AuctionStatus;

/**
 * Nhận NEW_BID / AUCTION_FINISHED từ ws://.../ws/auctions và tự nối lại khi rớt mạng.
 * Callback luôn chạy trên JavaFX thread. Dùng: Runnable huy = getInstance().subscribe(ev -> ...);
 */
public final class AuctionRealtimeClient extends BaseApiClient implements WebSocket.Listener {

    /** Khớp JSON AuctionEvent của server. type: NEW_BID | AUCTION_FINISHED (status CANCELED nếu admin dừng). */
    public record Event(String type, Long auctionId, BigDecimal currentPrice,
                        Long highestBidderId, AuctionStatus status, LocalDateTime endTime) {}

    private static final AuctionRealtimeClient INSTANCE = new AuctionRealtimeClient();
    public static AuctionRealtimeClient getInstance() { return INSTANCE; }

    private final List<Consumer<Event>> listeners = new CopyOnWriteArrayList<>();
    private final StringBuilder buffer = new StringBuilder();
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "auction-ws-retry");
        t.setDaemon(true);
        return t;
    });
    private volatile WebSocket socket;
    private volatile boolean running;

    private AuctionRealtimeClient() {}

    /** Đăng ký nhận event. Trả về Runnable để hủy đăng ký. */
    public Runnable subscribe(Consumer<Event> listener) {
        listeners.add(listener);
        start();
        return () -> listeners.remove(listener);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        connect();
    }

    public synchronized void stop() {
        running = false;
        WebSocket ws = socket;
        socket = null;
        if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
    }

    private void connect() {
        if (!running) return;
        reconnecting.set(false);
        String token = SessionData.getInstance().getToken();
        if (token == null || token.isBlank()) { // server bắt đăng nhập => chưa có token thì thử lại sau
            scheduleReconnect();
            return;
        }
        String url = BASE_URL.replaceFirst("^http", "ws") + "/ws/auctions"; // http->ws, https->wss
        HTTP.newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .header("Authorization", "Bearer " + token)
                .buildAsync(URI.create(url), this)
                .whenComplete((ws, err) -> {
                    if (err != null) scheduleReconnect();
                    else socket = ws;
                });
    }

    private void scheduleReconnect() {
        if (running && reconnecting.compareAndSet(false, true)) {
            retryExecutor.schedule(this::connect, 3, TimeUnit.SECONDS);
        }
    }

    // ---------- WebSocket.Listener ----------
    @Override public void onOpen(WebSocket ws) { socket = ws; ws.request(1); }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        buffer.append(data); // tin nhắn có thể bị chia nhiều mảnh
        if (last) {
            String json = buffer.toString();
            buffer.setLength(0);
            dispatch(json);
        }
        ws.request(1);
        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
        if (ws == socket) scheduleReconnect();
        return null;
    }

    @Override public void onError(WebSocket ws, Throwable error) { if (ws == socket) scheduleReconnect(); }

    private void dispatch(String json) {
        try {
            Event event = MAPPER.readValue(json, Event.class);
            Platform.runLater(() -> listeners.forEach(l -> l.accept(event)));
        } catch (Exception ignored) {
            // Event lạ / hỏng thì bỏ qua, không làm sập kết nối
        }
    }
}
