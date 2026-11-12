package vn.edu.auction.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import java.util.List;
import vn.edu.auction.client.api.AuthApiClient.ApiException;
import vn.edu.auction.shared.dto.ItemRequest;
import vn.edu.auction.shared.dto.ItemResponse;

public class ItemApiClient extends BaseApiClient {
    public List<ItemResponse> getAllItems() throws ApiException {
        return call("GET", "/api/items", null, new TypeReference<List<ItemResponse>>() {});
    }
    public ItemResponse getItemById(Long id) throws ApiException {
        return call("GET", "/api/items/" + id, null, new TypeReference<ItemResponse>() {});
    }
    public ItemResponse createItem(ItemRequest request) throws ApiException {
        return call("POST", "/api/items", request, new TypeReference<ItemResponse>() {});
    }
    public ItemResponse updateItem(Long id, ItemRequest request) throws ApiException {
        return call("PUT", "/api/items/" + id, request, new TypeReference<ItemResponse>() {});
    }
    public void deleteItem(Long id) throws ApiException {
        call("DELETE", "/api/items/" + id, null, null);
    }
}
