package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.AuctionService;
import vn.edu.auction.shared.dto.*;

@RestController
@RequestMapping("/api/auctions")
public class AuctionController {
    private final AuctionService auctions;

    public AuctionController(AuctionService auctions) { this.auctions = auctions; }

    @GetMapping
    public List<AuctionResponse> getAllAuctions() { return auctions.getAllAuctions(); }

    @GetMapping("/{id}")
    public AuctionResponse getAuctionById(@PathVariable("id") Long id) { return auctions.getAuctionById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuctionResponse createAuction(@RequestBody AuctionRequest request, Authentication auth) {
        return auctions.createAuction(request, AuctionPrincipal.userId(auth));
    }
}
