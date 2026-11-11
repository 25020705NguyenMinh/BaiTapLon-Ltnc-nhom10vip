package vn.edu.auction.server.service;

import java.math.BigDecimal;
import org.springframework.http.HttpStatus;
import vn.edu.auction.server.exception.AuctionBusinessException;

final class AuctionRules {
    private AuctionRules() {}

    static void require(boolean condition, HttpStatus status, String message) {
        if (!condition) throw new AuctionBusinessException(status, message);
    }

    static BigDecimal money(BigDecimal value) {
        require(value != null && value.signum() > 0, HttpStatus.BAD_REQUEST, "So tien phai lon hon 0.");
        require(value.stripTrailingZeros().scale() <= 2, HttpStatus.BAD_REQUEST, "So tien chi duoc co toi da 2 chu so thap phan.");
        require(value.compareTo(new BigDecimal("99999999999999999.99")) <= 0,
                HttpStatus.BAD_REQUEST, "So tien vuot gioi han luu tru.");
        return value.setScale(2);
    }
}
