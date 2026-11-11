package vn.edu.auction.client;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * FILE MỚI - quản lý ngôn ngữ hiện tại của ứng dụng.
 * Mọi nơi cần lấy chữ theo ngôn ngữ đều gọi I18n.get("key").
 */
public final class I18n {

    // Thư mục resources/vn/edu/auction/client/i18n/ chứa messages_vi.properties / messages_en.properties
    // (không có đuôi _vi/_en và không có .properties)
    private static final String BASE_NAME = "vn.edu.auction.client.i18n.messages";

    public static final Locale VIETNAMESE = Locale.forLanguageTag("vi");

    private static Locale currentLocale = VIETNAMESE; // ngôn ngữ mặc định

    private I18n() {
    }

    public static Locale getLocale() {
        return currentLocale;
    }

    public static void setLocale(Locale locale) {
        currentLocale = locale;
    }

    public static boolean isVietnamese() {
        return "vi".equals(currentLocale.getLanguage());
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BASE_NAME, currentLocale);
    }

    public static String get(String key) {
        return getBundle().getString(key);
    }
}
