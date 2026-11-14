package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import vn.edu.auction.shared.dto.*;

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
 * Controller chỉ gọi login()/register()/verifyEmail()... mà không phải biết URL, JSON hay mã HTTP.
 */
public class AuthApiClient {

    private static final String DEFAULT_BASE_URL = "https://baitaplon-ltnc-nhom10vip.onrender.com";
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public AuthApiClient() {
        this(System.getProperty("auction.server.url", DEFAULT_BASE_URL));
    }

    public AuthApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    // ===================== CÁC API =====================

    /**
     * POST /api/auth/register.
     * VÌ SAO không gọi requireToken: từ khi có OTP, đăng ký xong server CHƯA cấp token
     * (phải xác minh email trước), nên AuthResponse ở đây có token = null.
     */
    public AuthResponse register(RegisterRequest request) throws ApiException {
        requireNotNull(request);
        return post("/api/auth/register", request, AuthResponse.class);
    }

    /** POST /api/auth/verify-email: đúng OTP thì server trả AuthResponse có token. */
    public AuthResponse verifyEmail(String email, String otp) throws ApiException {
        AuthResponse response = post("/api/auth/verify-email",
                new VerifyOtpRequest(email, otp), AuthResponse.class);
        return requireToken(response);
    }

    /** POST /api/auth/resend-otp. */
    public MessageResponse resendOtp(String email) throws ApiException {
        return post("/api/auth/resend-otp", new EmailRequest(email), MessageResponse.class);
    }

    /** POST /api/auth/login. */
    public AuthResponse login(LoginRequest request) throws ApiException {
        requireNotNull(request);
        return requireToken(post("/api/auth/login", request, AuthResponse.class));
    }

    /** GET /api/auth/me với header "Authorization: Bearer <token>". */
    public AuthResponse getCurrentUser(String token) throws ApiException {
        if (token == null || token.isBlank()) {
            throw new ApiException(ApiException.Type.UNAUTHORIZED, 401, null, null);
        }
        HttpRequest httpRequest = requestBuilder("/api/auth/me")
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();
        return requireToken(send(httpRequest, AuthResponse.class));
    }

    // ===================== HÀM NỘI BỘ =====================

    private <T> T post(String path, Object body, Class<T> responseType) throws ApiException {
        String json;
        try {
            json = objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new ApiException(ApiException.Type.UNKNOWN, 0, null, e);
        }

        HttpRequest request = requestBuilder(path)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return send(request, responseType);
    }

    private HttpRequest.Builder requestBuilder(String path) {
        return HttpRequest.newBuilder(URI.create(baseUrl + path))
                .timeout(REQUEST_TIMEOUT)
                .header("Accept", "application/json");
    }

    // Thành công -> trả đối tượng theo responseType; thất bại -> ném ApiException có phân loại.
    private <T> T send(HttpRequest request, Class<T> responseType) throws ApiException {
        HttpResponse<String> response;
        try {
            response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        } catch (HttpTimeoutException e) {
            throw new ApiException(ApiException.Type.TIMEOUT, 0, null, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(ApiException.Type.UNKNOWN, 0, null, e);
        } catch (IOException e) {
            throw new ApiException(ApiException.Type.NETWORK, 0, null, e);
        }

        int status = response.statusCode();
        if (status >= 200 && status < 300) {
            return parseBody(response.body(), responseType);
        }
        throw toApiException(status, response.body());
    }

    private <T> T parseBody(String body, Class<T> responseType) throws ApiException {
        try {
            T result = objectMapper.readValue(body, responseType);
            if (result == null) {
                throw new ApiException(ApiException.Type.UNKNOWN, 200, null, null);
            }
            return result;
        } catch (JsonProcessingException e) {
            throw new ApiException(ApiException.Type.UNKNOWN, 200, null, e);
        }
    }

    // Đăng nhập / xác minh / me mà không có token là bất thường -> báo lỗi thay vì tạo session hỏng
    private AuthResponse requireToken(AuthResponse response) throws ApiException {
        if (response.getToken() == null || response.getToken().isBlank()) {
            throw new ApiException(ApiException.Type.UNKNOWN, 200, null, null);
        }
        return response;
    }

    private ApiException toApiException(int status, String body) {
        ApiException.Type type;
        switch (status) {
            case 400 -> type = ApiException.Type.BAD_REQUEST;
            case 401 -> type = ApiException.Type.UNAUTHORIZED;
            case 403 -> type = ApiException.Type.FORBIDDEN;
            case 409 -> type = ApiException.Type.CONFLICT;
            case 429 -> type = ApiException.Type.RATE_LIMITED;
            default -> type = status >= 500 ? ApiException.Type.SERVER : ApiException.Type.UNKNOWN;
        }
        // Chỉ lấy message của server với lỗi 4xx; lỗi 5xx không lấy để khỏi lộ chi tiết nội bộ
        String serverMessage = (status >= 400 && status < 500) ? extractMessage(body) : null;
        return new ApiException(type, status, serverMessage, null);
    }

    private String extractMessage(String body) {
        try {
            String message = objectMapper.readTree(body).path("message").asText(null);
            return (message == null || message.isBlank()) ? null : message;
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private void requireNotNull(Object request) {
        if (request == null) {
            throw new IllegalArgumentException("Request không được null");
        }
    }

    // ===================== NGOẠI LỆ CỦA API =====================

    public static class ApiException extends Exception {

        private static final long serialVersionUID = 1L;

        public enum Type {
            BAD_REQUEST,   // 400: dữ liệu sai (kể cả nhập sai OTP)
            UNAUTHORIZED,  // 401: sai email/mật khẩu hoặc token không hợp lệ
            FORBIDDEN,     // 403: tài khoản chưa xác minh / bị khóa
            CONFLICT,      // 409: email đã tồn tại
            RATE_LIMITED,  // 429: xin mã OTP quá nhanh
            SERVER,        // 5xx: lỗi phía server
            NETWORK,       // không kết nối được
            TIMEOUT,       // server phản hồi quá lâu
            UNKNOWN
        }

        private final Type type;
        private final int statusCode;
        private final String serverMessage;

        public ApiException(Type type, int statusCode, String serverMessage, Throwable cause) {
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
    public MessageResponse forgotPassword(String email) throws ApiException {
        return post("/api/auth/forgot-password", new EmailRequest(email), MessageResponse.class);
    }

    public MessageResponse resetPassword(String email, String otp, String newPassword) throws ApiException {
        return post("/api/auth/reset-password", new ResetPasswordRequest(email, otp, newPassword), MessageResponse.class);
    }
}