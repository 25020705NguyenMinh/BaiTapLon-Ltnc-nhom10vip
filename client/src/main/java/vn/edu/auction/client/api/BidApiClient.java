package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import vn.edu.auction.client.api.AuthApiClient.ApiException;
import vn.edu.auction.shared.dto.*;

public class BidApiClient extends BaseApiClient {
    public BidResponse placeBid(Long auctionId, PlaceBidRequest request) throws ApiException {
        return call("POST", "/api/auctions/" + auctionId + "/bids", request, new TypeReference<BidResponse>() {});
    }
    public List<BidResponse> getBidHistory(Long auctionId) throws ApiException {
        return call("GET", "/api/auctions/" + auctionId + "/bids", null, new TypeReference<List<BidResponse>>() {});
    }
}