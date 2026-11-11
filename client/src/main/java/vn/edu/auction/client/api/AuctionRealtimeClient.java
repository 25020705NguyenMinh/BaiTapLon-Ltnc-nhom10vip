package vn.edu.auction.client.api;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import javafx.application.Platform;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.enums.AuctionStatus;

public final class AuctionRealtimeClient extends BaseApiClient implements WebSocket.Listener {

    /** Khớp JSON của AuctionEvent phía server (type: NEW_BID | AUCTION_FINISHED). */
    public record Event(String type, Long auctionId, BigDecimal currentPrice,
                        Long highestBidderId, AuctionStatus status, LocalDateTime endTime) {}

    private static final AuctionRealtimeClient INSTANCE = new AuctionRealtimeClient();
    public static AuctionRealtimeClient getInstance() { return INSTANCE; }

    private final List<Consumer<Event>> listeners = new CopyOnWriteArrayList<>();
    private final StringBuilder buffer = new StringBuilder();
    private final AtomicBoolean reconnecting = new AtomicBoolean(false);
    private final ScheduledExecutorService retryExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ws-reconnect");
        t.setDaemon(true);
        return t;
    });
    private volatile WebSocket socket;
    private volatile boolean running;

    private AuctionRealtimeClient() {}

    /** Đăng ký nhận event (callback chạy trên JavaFX thread). Trả về Runnable để hủy đăng ký. */
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
        if (ws != null) ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
    }

    private void connect() {
        if (!running) return;
        reconnecting.set(false);
        String url = BASE_URL.replaceFirst("^http", "ws") + "/ws/auctions"; // http->ws, https->wss
        WebSocket.Builder builder = HTTP.newWebSocketBuilder().connectTimeout(Duration.ofSeconds(5));
        String token = SessionData.getInstance().getToken();
        if (token != null && !token.isBlank()) builder.header("Authorization", "Bearer " + token);

        builder.buildAsync(URI.create(url), this).whenComplete((ws, err) -> {
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
        scheduleReconnect();
        return null;
    }

    @Override public void onError(WebSocket ws, Throwable error) { scheduleReconnect(); }

    private void dispatch(String json) {
        try {
            Event event = MAPPER.readValue(json, Event.class);
            Platform.runLater(() -> listeners.forEach(l -> l.accept(event)));
        } catch (Exception ignored) {
            // Event lạ/hỏng thì bỏ qua, không làm sập kết nối
        }
    }
}