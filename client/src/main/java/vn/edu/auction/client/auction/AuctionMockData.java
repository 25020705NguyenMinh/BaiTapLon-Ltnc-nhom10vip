package vn.edu.auction.client.auction;

import vn.edu.auction.shared.dto.AuctionResponse;
import vn.edu.auction.shared.dto.BidResponse;
import vn.edu.auction.shared.dto.ItemResponse;
import vn.edu.auction.shared.enums.AuctionStatus;
import vn.edu.auction.shared.enums.BidStatus;
import vn.edu.auction.shared.enums.ItemStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dữ liệu giả lập dùng chung cho ba màn hình P4.
 * Giai đoạn này chưa kết nối API Backend.
 */
public final class AuctionMockData {

    private static final Long AUCTION_ID = 1L;
    private static final Long CURRENT_USER_ID = 999L;

    // Thông tin sản phẩm mẫu
    private static final ItemResponse ITEM = new ItemResponse(
            101L,
            301L,
            "Laptop Dell XPS 13",
            "Dell XPS 13, màn hình 13.4 inch, thiết kế mỏng nhẹ. " +
                    "Máy hoạt động tốt, phù hợp cho học tập và công việc văn phòng.",
            new BigDecimal("18000000"),
            ItemStatus.ACTIVE
    );

    // Thông tin phiên đấu giá mẫu
    private static final AuctionResponse AUCTION = new AuctionResponse(
            AUCTION_ID,
            ITEM.getId(),
            ITEM.getName(),
            new BigDecimal("18000000"),
            new BigDecimal("19500000"),
            202L,
            LocalDateTime.now().minusHours(2),
            LocalDateTime.now().plusHours(4),
            AuctionStatus.RUNNING
    );

    // Lịch sử đặt giá mẫu
    private static final List<BidResponse> BIDS = new ArrayList<>(List.of(
            new BidResponse(
                    1L, AUCTION_ID, 201L,
                    new BigDecimal("18500000"),
                    LocalDateTime.now().minusMinutes(45),
                    BidStatus.ACCEPTED,
                    "Đặt giá thành công"
            ),
            new BidResponse(
                    2L, AUCTION_ID, 202L,
                    new BigDecimal("19000000"),
                    LocalDateTime.now().minusMinutes(25),
                    BidStatus.ACCEPTED,
                    "Đặt giá thành công"
            ),
            new BidResponse(
                    3L, AUCTION_ID, 202L,
                    new BigDecimal("19500000"),
                    LocalDateTime.now().minusMinutes(10),
                    BidStatus.ACCEPTED,
                    "Đang dẫn đầu"
            )
    ));

    private AuctionMockData() {
        // Không cho phép tạo đối tượng từ lớp dữ liệu mẫu này.
    }

    // Lấy thông tin phiên đấu giá
    public static AuctionResponse getAuction(Long auctionId) {
        return AUCTION;
    }

    // Lấy thông tin sản phẩm
    public static ItemResponse getItem() {
        return ITEM;
    }

    // Lấy lịch sử đặt giá
    public static List<BidResponse> getBids(Long auctionId) {
        return new ArrayList<>(BIDS);
    }

    // Thêm một lượt đặt giá mới
    public static void addBid(BigDecimal amount) {
        long nextId = BIDS.stream()
                .mapToLong(BidResponse::getBidId)
                .max()
                .orElse(0L) + 1;

        BidResponse bid = new BidResponse(
                nextId,
                AUCTION_ID,
                CURRENT_USER_ID,
                amount,
                LocalDateTime.now(),
                BidStatus.ACCEPTED,
                "Đặt giá thành công"
        );

        BIDS.add(bid);

        // Cập nhật giá hiện tại và người đang dẫn đầu
        AUCTION.setCurrentPrice(amount);
        AUCTION.setHighestBidderId(CURRENT_USER_ID);
    }

    // Lấy tên người đặt giá từ ID mẫu
    public static String bidderName(Long bidderId) {
        if (bidderId == null) {
            return "Không xác định";
        }

        if (bidderId.equals(CURRENT_USER_ID)) {
            return "Bạn";
        }

        if (bidderId.equals(201L)) {
            return "Nguyễn Văn An";
        }

        if (bidderId.equals(202L)) {
            return "Trần Minh Bình";
        }

        return "Người dùng #" + bidderId;
    }

    // Lấy tên người đang thắng đấu giá
    public static String currentWinnerName() {
        return bidderName(AUCTION.getHighestBidderId());
    }
}
