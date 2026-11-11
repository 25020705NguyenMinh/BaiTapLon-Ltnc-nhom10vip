package vn.edu.auction.client.api;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
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

/**
 * Kết nối ws://.../ws/user: nhận thông báo và tin nhắn gửi RIÊNG cho người đang đăng nhập.
 * Gói tin: {"kind":"NOTIFICATION"|"CHAT","payload":{...}}. Tự nối lại khi rớt mạng.
 */
public final class UserSocketClient extends BaseApiClient implements WebSocket.Listener {
    private static final UserSocketClient INSTANCE = new UserSocketClient();
    public static UserSocketClient getInstance() { return INSTANCE; }

    private final List<Consumer<JsonNode>> listeners = new CopyOnWriteArrayList<>();
    private final StringBuilder buffer = new StringBuilder();
    private final AtomicBoolean retrying = new AtomicBoolean();
    private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "user-ws-retry");
        t.setDaemon(true);
        return t;
    });
    private volatile WebSocket ws;
    private volatile boolean running;

    private UserSocketClient() {}

    /** Callback chạy trên JavaFX thread. Trả về Runnable để hủy đăng ký. */
    public Runnable subscribe(Consumer<JsonNode> l) {
        listeners.add(l);
        return () -> listeners.remove(l);
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        connect();
    }

    public synchronized void stop() {
        running = false;
        WebSocket w = ws;
        ws = null;
        if (w != null) w.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
    }

    private void connect() {
        if (!running) return;
        retrying.set(false);
        String token = SessionData.getInstance().getToken();
        if (token == null || token.isBlank()) return;
        String url = BASE_URL.replaceFirst("^http", "ws") + "/ws/user";
        HTTP.newWebSocketBuilder()
                .header("Authorization", "Bearer " + token)
                .connectTimeout(Duration.ofSeconds(5))
                .buildAsync(URI.create(url), this)
                .whenComplete((w, err) -> {
                    if (err != null) retry();
                    else ws = w;
                });
    }

    private void retry() {
        if (running && retrying.compareAndSet(false, true)) timer.schedule(this::connect, 3, TimeUnit.SECONDS);
    }

    @Override public void onOpen(WebSocket w) { w.request(1); }

    @Override
    public CompletionStage<?> onText(WebSocket w, CharSequence data, boolean last) {
        buffer.append(data);
        if (last) {
            String json = buffer.toString();
            buffer.setLength(0);
            try {
                JsonNode node = MAPPER.readTree(json);
                Platform.runLater(() -> listeners.forEach(l -> l.accept(node)));
            } catch (Exception ignored) {
                // gói tin hỏng thì bỏ qua
            }
        }
        w.request(1);
        return null;
    }

    @Override public CompletionStage<?> onClose(WebSocket w, int code, String reason) { if (w == ws) retry(); return null; }
    @Override public void onError(WebSocket w, Throwable e) { if (w == ws) retry(); }
}
