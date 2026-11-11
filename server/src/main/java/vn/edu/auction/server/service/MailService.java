package vn.edu.auction.server.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.edu.auction.server.exception.EmailSendException;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    // VÌ SAO dùng ObjectProvider: nếu chưa cấu hình spring.mail.host thì Spring không tạo
    // JavaMailSender; inject thẳng sẽ làm server không khởi động được.
    // Dùng ObjectProvider thì chưa cấu hình vẫn chạy, chỉ in mã ra console (chế độ dev).
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final String from;

    public MailService(ObjectProvider<JavaMailSender> mailSenderProvider,
                       @Value("${app.mail.from}") String from) {
        this.mailSenderProvider = mailSenderProvider;
        this.from = from;
    }

    public void sendOtp(String to, String code, long expirationMinutes) {
        JavaMailSender sender = mailSenderProvider.getIfAvailable();

        if (sender == null) {
            // CHỈ dành cho dev. Khi chạy thật phải cấu hình SMTP để mã không nằm trong log.
            log.warn("[DEV] Chưa cấu hình SMTP. Mã OTP của {} là: {}", to, code);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("[Tech Auction] Mã xác minh của bạn");
        message.setText("Mã xác minh của bạn là: " + code
                + "\n\nMã có hiệu lực trong " + expirationMinutes + " phút."
                + "\nNếu bạn không thực hiện yêu cầu này, hãy bỏ qua email.");

        try {
            sender.send(message);
        } catch (MailException e) {
            // Ghi chi tiết ở log server, người dùng chỉ nhận câu chung (không lộ thông tin SMTP)
            log.error("Gửi email OTP tới {} thất bại", to, e);
            throw new EmailSendException(e);
        }
    }
}