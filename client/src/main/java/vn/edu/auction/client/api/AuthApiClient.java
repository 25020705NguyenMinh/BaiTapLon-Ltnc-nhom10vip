package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.auction.shared.dto.AuthResponse;
import vn.edu.auction.shared.dto.LoginRequest;
import vn.edu.auction.shared.dto.RegisterRequest;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Lớp duy nhất trong client biết cách "nói chuyện" với API xác thực của server.
 *
 * VÌ SAO TÁCH RIÊNG: Controller (giao diện) chỉ cần gọi login()/register(), không phải biết
 * URL, JSON, mã lỗi HTTP... Sau này đổi địa chỉ server hay đổi cách gọi chỉ sửa ở đây.
 */
public class AuthApiClient {

    // Địa chỉ server mặc định. Có thể đổi khi chạy mà không cần sửa code:
    // thêm VM option  -Dauction.server.url=http://192.168.1.10:8080
    // VÌ SAO: khi demo nhóm, server chạy máy khác thì client trỏ được tới đó.
    private static final String DEFAULT_BASE_URL = "http://localhost:8080";

    // Có timeout để server tắt/treo thì người dùng nhận thông báo, thay vì chờ mãi.
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public AuthApiClient() {
        this(System.getProperty("auction.server.url", DEFAULT_BASE_URL));
    }

    public AuthApiClient(String baseUrl) {
        // Bỏ dấu "/" cuối để ghép baseUrl + "/api/..." không bị thành "//api"
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        this.httpClient = HttpClient.newBuilder()
                // VÌ SAO HTTP/1.1: mặc định HttpClient thử nâng cấp lên HTTP/2 trên cổng thường,
                // đôi khi gây lỗi lạ với server nhúng. Ép HTTP/1.1 cho ổn định.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();

        // VÌ SAO tắt FAIL_ON_UNKNOWN_PROPERTIES: nếu server sau này thêm field mới vào response,
        // client cũ vẫn đọc được, không bị văng lỗi.
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== 3 METHOD CHÍNH =====================

    /** POST /api/auth/register (server trả 201 + AuthResponse). */
    public AuthResponse register(RegisterRequest request) throws ApiException {
        requireNotNull(request);
        return post("/api/auth/register", request);
    }

    /** POST /api/auth/login (server trả 200 + AuthResponse chứa JWT). */
    public AuthResponse login(LoginRequest request) throws ApiException {
        requireNotNull(request);
        return post("/api/auth/login", request);
    }

    /** GET /api/auth/me với header "Authorization: Bearer <token>". */
    public AuthResponse getCurrentUser(String token) throws ApiException {
        if (token == null || token.isBlank()) {
            // Không có token thì khỏi gọi mạng, coi như chưa đăng nhập
            throw new ApiException(ApiException.Type.UNAUTHORIZED, 401, null, null);
        }
        HttpRequest httpRequest = requestBuilder("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return send(httpRequest);
    }

    // ===================== HÀM NỘI BỘ =====================

    private AuthResponse post(String path, Object body) throws ApiException {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            // Lỗi do code (DTO không chuyển được JSON), không phải lỗi người dùng.
            throw new ApiException(ApiException.Type.UNKNOWN, 0, null, e);
        }

        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8)) // UTF-8 để tên tiếng Việt không bị lỗi
                .build();
        return send(request);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
    }

    /**
     * Gửi request rồi "phiên dịch" mọi kết quả về 2 khả năng:
     * thành công -> trả AuthResponse; thất bại -> ném ApiException có phân loại rõ.
     */
    private AuthResponse send(HttpRequest request) throws ApiException {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            // Phải catch TRƯỚC IOException vì nó là con của IOException
            throw new ApiException(ApiException.Type.TIMEOUT, 0, null, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // giữ lại tín hiệu ngắt luồng, không được nuốt
            throw new ApiException(ApiException.Type.UNKNOWN, 0, null, e);
        } catch (IOException e) {
            // Server tắt / sai địa chỉ / mất mạng (ConnectException, UnresolvedAddressException...)
            throw new ApiException(ApiException.Type.NETWORK, 0, null, e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return parseAuthResponse(response.body());
        }
        throw toApiException(status, response.body());
    }

    private AuthResponse parseAuthResponse(String body) throws ApiException {
        try {
            AuthResponse result = objectMapper.readValue(body, AuthResponse.class);
            // Server trả 2xx mà thiếu token là bất thường, báo lỗi thay vì để app chạy tiếp với session hỏng
            if (result == null || result.getToken() == null || result.getToken().isBlank()) {
                throw new ApiException(ApiException.Type.UNKNOWN, 200, null, null);
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new ApiException(ApiException.Type.UNKNOWN, 200, null, e);
        }
    }

    // Đổi mã HTTP lỗi thành loại lỗi dễ xử lý ở giao diện
    private ApiException toApiException(int status, String body) {
        ApiException.Type type;
        switch (status) {
            case 400 -> type = ApiException.Type.BAD_REQUEST;
            case 401 -> type = ApiException.Type.UNAUTHORIZED;
            case 403 -> type = ApiException.Type.FORBIDDEN;
            case 409 -> type = ApiException.Type.CONFLICT;
            default -> type = status >= 500 ? ApiException.Type.SERVER : ApiException.Type.UNKNOWN;
        }
        // Chỉ lấy message của server với lỗi 4xx (do server mình viết, an toàn để hiển thị).
        // Lỗi 5xx không lấy: tránh lộ chi tiết nội bộ lên giao diện.
        String serverMessage = (status >= 400 && status < 500) ? extractMessage(body) : null;
        return new ApiException(type, status, serverMessage, null);
    }

    // Đọc trường "message" trong ApiErrorResponse của server.
    // VÌ SAO dùng JsonNode mà không map sang ApiErrorResponse: lớp đó có LocalDateTime,
    // đọc nó phải thêm module jsr310. Ta chỉ cần message nên đọc thẳng cho nhẹ.
    private String extractMessage(String body) {
        try {
            String message = objectMapper.readTree(body).path("message").asText(null);
            return (message == null || message.isBlank()) ? null : message;
        } catch (JsonProcessingException e) {
            return null; // body không phải JSON -> dùng câu mặc định ở giao diện
        }
    }

    private void requireNotNull(Object request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không được null");
        }
    }

    // ===================== NGOẠI LỆ CỦA API =====================

    /**
     * Mọi lỗi khi gọi API đều ra dưới dạng này.
     * VÌ SAO: giao diện chỉ cần xem getType() để chọn câu thông báo, không phải đụng tới
     * HttpResponse hay exception của thư viện. Exception gốc nằm trong cause để ghi log.
     */
    public static class ApiException extends Exception {

        private static final long serialVersionUID = 1L;

        public enum Type {
            BAD_REQUEST,   // 400: dữ liệu gửi lên sai
            UNAUTHORIZED,  // 401: sai email/mật khẩu hoặc token không hợp lệ
            FORBIDDEN,     // 403: tài khoản bị khóa
            CONFLICT,      // 409: email đã tồn tại
            SERVER,        // 5xx: lỗi phía server
            NETWORK,       // không kết nối được (server tắt, mất mạng)
            TIMEOUT,       // server phản hồi quá lâu
            UNKNOWN        // phản hồi lạ / lỗi không phân loại được
        }

        private final Type type;
        private final int statusCode;
        private final String serverMessage;

        public ApiException(Type type, int statusCode, String serverMessage, Throwable cause) {
            // Message này chỉ để ghi log cho lập trình viên, KHÔNG hiển thị lên giao diện
            super("API lỗi [" + type + ", HTTP " + statusCode + "]"
                    + (serverMessage != null ? ": " + serverMessage : ""), cause);
            this.type = type;
            this.statusCode = statusCode;
            this.serverMessage = serverMessage;
        }

        public Type getType() { return type; }
        public int getStatusCode() { return statusCode; }
        public String getServerMessage() { return serverMessage; }
    }
}