package vn.edu.auction.client.ui;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.HashMap;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.StringConverter;
import vn.edu.auction.client.api.BaseApiClient;
import vn.edu.auction.client.api.FxAsync;
import vn.edu.auction.client.api.UserApiClient;
import vn.edu.auction.client.api.UserSocketClient;
import vn.edu.auction.client.model.SessionData;
import vn.edu.auction.shared.dto.ExtraDto.ChatMessageResponse;
import vn.edu.auction.shared.dto.ExtraDto.UserBriefResponse;

/** Cửa sổ chat nhỏ ở góc dưới bên phải. Gửi qua REST, nhận realtime qua /ws/user. */
public final class ChatWindow {
    private static ChatWindow instance;

    public static void toggle(Window owner) {
        if (instance != null && instance.stage.isShowing()) { close(); return; }
        instance = new ChatWindow(owner);
    }

    public static void close() {
        if (instance != null) { instance.dispose(); instance = null; }
    }

    private final UserApiClient api = new UserApiClient();
    private final Stage stage = new Stage();
    private final ComboBox<UserBriefResponse> partner = new ComboBox<>();
    private final ListView<ChatMessageResponse> list = new ListView<>();
    private final TextField input = new TextField();
    private final Label hint = new Label();
    private final Map<Long, String> names = new HashMap<>();
    private Runnable unsub;

    private ChatWindow(Window owner) {
        stage.initOwner(owner);
        stage.initStyle(StageStyle.UTILITY);
        stage.setTitle("Tin nhắn");

        partner.setPromptText("Chọn người để nhắn...");
        partner.setMaxWidth(Double.MAX_VALUE);
        partner.setConverter(new StringConverter<>() {
            @Override public String toString(UserBriefResponse u) { return u == null ? "" : u.fullName() + " (" + u.role() + ")"; }
            @Override public UserBriefResponse fromString(String s) { return null; }
        });
        partner.valueProperty().addListener((o, a, u) -> loadConversation(u));

        Long me = SessionData.getInstance().getUserId();
        list.setPlaceholder(new Label("Chọn người để bắt đầu trò chuyện."));
        list.setCellFactory(v -> new ListCell<>() {
            @Override protected void updateItem(ChatMessageResponse m, boolean empty) {
                super.updateItem(m, empty);
                if (empty || m == null) { setText(null); return; }
                setText(m.content());
                setWrapText(true);
                setPrefWidth(300);
                setAlignment(m.senderId().equals(me) ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
            }
        });

        Button send = new Button("Gửi");
        send.setOnAction(e -> send());
        input.setOnAction(e -> send());
        HBox row = new HBox(6, input, send);
        HBox.setHgrow(input, Priority.ALWAYS);
        VBox root = new VBox(8, partner, list, row, hint);
        root.setPadding(new Insets(10));
        VBox.setVgrow(list, Priority.ALWAYS);
        Scene scene = new Scene(root, 340, 440);
        UiTheme.apply(scene);
        stage.setScene(scene);
        // góc dưới bên phải cửa sổ chính
        stage.setX(owner.getX() + owner.getWidth() - 340 - 20);
        stage.setY(owner.getY() + owner.getHeight() - 440 - 40);

        FxAsync.run(api::directory, users -> {
            users.forEach(u -> names.put(u.id(), u.fullName()));
            partner.getItems().setAll(users);
        }, e -> hint.setText(BaseApiClient.describe(e)));
        unsub = UserSocketClient.getInstance().subscribe(this::onSocket);
        stage.setOnHidden(e -> dispose());
        stage.show();
    }

    private void loadConversation(UserBriefResponse u) {
        list.getItems().clear();
        if (u == null) return;
        FxAsync.run(() -> api.chat(u.id()), msgs -> { list.getItems().setAll(msgs); scrollEnd(); },
                e -> hint.setText(BaseApiClient.describe(e)));
    }

    private void send() {
        UserBriefResponse to = partner.getValue();
        String text = input.getText().trim();
        if (to == null || text.isEmpty()) return;
        input.clear();
        FxAsync.run(() -> api.sendChat(to.id(), text), m -> { list.getItems().add(m); scrollEnd(); },
                e -> hint.setText(BaseApiClient.describe(e)));
    }

    private void onSocket(JsonNode msg) {
        if (!"CHAT".equals(msg.path("kind").asText())) return;
        try {
            ChatMessageResponse m = BaseApiClient.MAPPER.treeToValue(msg.get("payload"), ChatMessageResponse.class);
            UserBriefResponse cur = partner.getValue();
            if (cur != null && cur.id().equals(m.senderId())) {
                list.getItems().add(m);
                scrollEnd();
            } else {
                hint.setText("Tin nhắn mới từ " + names.getOrDefault(m.senderId(), "#" + m.senderId()));
            }
        } catch (Exception ignored) {
            // gói tin hỏng thì bỏ qua
        }
    }

    private void scrollEnd() {
        if (!list.getItems().isEmpty()) list.scrollTo(list.getItems().size() - 1);
    }

    private void dispose() {
        if (unsub != null) { unsub.run(); unsub = null; }
        stage.close();
    }
}
