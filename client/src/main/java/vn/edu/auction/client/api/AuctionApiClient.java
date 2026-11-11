package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import vn.edu.auction.client.api.AuthApiClient.ApiException;
import vn.edu.auction.shared.dto.*;

public class AuctionApiClient extends BaseApiClient {
    public List<AuctionResponse> getAllAuctions() throws ApiException {
        return call("GET", "/api/auctions", null, new TypeReference<List<AuctionResponse>>() {});
    }
    public AuctionResponse getAuctionById(Long id) throws ApiException {
        return call("GET", "/api/auctions/" + id, null, new TypeReference<AuctionResponse>() {});
    }
    public AuctionResponse createAuction(AuctionRequest request) throws ApiException {
        return call("POST", "/api/auctions", request, new TypeReference<AuctionResponse>() {});
    }
}