package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.BidService;
import vn.edu.auction.shared.dto.*;

@RestController
@RequestMapping("/api/auctions/{id}/bids")
public class BidController {
    private final BidService bids;

    public BidController(BidService bids) { this.bids = bids; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BidResponse placeBid(@PathVariable("id") Long id, @RequestBody PlaceBidRequest request, Authentication auth) {
        return bids.placeBid(id, AuctionPrincipal.userId(auth), request);
    }

    @GetMapping
    public List<BidResponse> getBidHistory(@PathVariable("id") Long id) { return bids.getBidHistory(id); }
}
