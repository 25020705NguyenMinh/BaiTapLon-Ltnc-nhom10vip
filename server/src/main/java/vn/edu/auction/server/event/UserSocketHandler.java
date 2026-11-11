package vn.edu.auction.server.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * WebSocket /ws/user: đẩy tin RIÊNG cho từng người dùng (thông báo + chat).
 * userId lấy từ JWT của lúc handshake (principal của JwtAuthenticationFilter là Long).
 * Gói tin: {"kind":"NOTIFICATION"|"CHAT","payload":{...}}
 */
@Component
public class UserSocketHandler extends TextWebSocketHandler {
    private final Map<Long, Map<String, WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper;

    public UserSocketHandler(ObjectMapper mapper) { this.mapper = mapper; }

    private static Long userId(WebSocketSession s) {
        try {
            return s.getPrincipal() == null ? null : Long.valueOf(s.getPrincipal().getName());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession s) throws Exception {
        Long id = userId(s);
        if (id == null) { s.close(CloseStatus.POLICY_VIOLATION); return; }
        sessions.computeIfAbsent(id, k -> new ConcurrentHashMap<>())
                .put(s.getId(), new ConcurrentWebSocketSessionDecorator(s, 5000, 64 * 1024));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession s, CloseStatus status) { drop(s); }

    @Override
    public void handleTransportError(WebSocketSession s, Throwable ex) { drop(s); }

    private void drop(WebSocketSession s) {
        Long id = userId(s);
        if (id != null && sessions.get(id) != null) sessions.get(id).remove(s.getId());
    }

    /** Chỉ gửi sau khi transaction hiện tại commit (không có transaction thì gửi ngay). */
    public void pushAfterCommit(Long userId, String kind, Object payload) {
        Runnable r = () -> push(userId, kind, payload);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() { r.run(); }
            });
        } else {
            r.run();
        }
    }

    private void push(Long userId, String kind, Object payload) {
        Map<String, WebSocketSession> userSessions = sessions.get(userId);
        if (userSessions == null) return;
        TextMessage msg;
        try {
            msg = new TextMessage(mapper.writeValueAsString(Map.of("kind", kind, "payload", payload)));
        } catch (IOException e) {
            return;
        }
        userSessions.values().forEach(s -> {
            try {
                if (s.isOpen()) s.sendMessage(msg);
            } catch (Exception e) {
                userSessions.remove(s.getId());
            }
        });
    }
}
