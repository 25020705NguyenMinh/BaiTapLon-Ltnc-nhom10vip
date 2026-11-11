package vn.edu.auction.server.service;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.edu.auction.server.entity.ItemEntity;
import vn.edu.auction.server.repository.*;
import vn.edu.auction.shared.dto.*;
import vn.edu.auction.shared.enums.*;

@Service
@Transactional(readOnly = true)
public class ItemService {
    private final ItemRepository items;
    private final AuctionRepository auctions;
    private final AuctionUserService users;

    public ItemService(ItemRepository items, AuctionRepository auctions, AuctionUserService users) {
        this.items = items;
        this.auctions = auctions;
        this.users = users;
    }

    public List<ItemResponse> getAllItems() {
        return items.findAll().stream().filter(i -> i.getStatus() != ItemStatus.REMOVED).map(this::response).toList();
    }

    public ItemResponse getItemById(Long id) { return response(findItem(id)); }

    @Transactional
    public ItemResponse createItem(ItemRequest request, Long sellerId) {
        users.requireRole(sellerId, UserRole.SELLER);
        ItemEntity item = new ItemEntity();
        item.setSellerId(sellerId);
        item.setStatus(ItemStatus.DRAFT);
        apply(item, request);
        return response(items.save(item));
    }

    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request, Long sellerId) {
        ItemEntity item = editableItem(id, sellerId);
        apply(item, request);
        return response(item);
    }

    @Transactional
    public void deleteItem(Long id, Long sellerId) {
        editableItem(id, sellerId).setStatus(ItemStatus.REMOVED);
    }

    private ItemEntity editableItem(Long id, Long sellerId) {
        users.requireRole(sellerId, UserRole.SELLER);
        ItemEntity item = items.findByIdForUpdate(id).orElse(null);
        AuctionRules.require(item != null && item.getStatus() != ItemStatus.REMOVED, HttpStatus.NOT_FOUND, "Khong tim thay san pham.");
        AuctionRules.require(item.getSellerId().equals(sellerId), HttpStatus.FORBIDDEN, "Ban khong so huu san pham nay.");
        AuctionRules.require(!auctions.existsByItemId(id), HttpStatus.CONFLICT, "San pham da co phien dau gia, khong the sua hoac xoa.");
        return item;
    }

    private ItemEntity findItem(Long id) {
        ItemEntity item = items.findById(id).orElse(null);
        AuctionRules.require(item != null && item.getStatus() != ItemStatus.REMOVED, HttpStatus.NOT_FOUND, "Khong tim thay san pham.");
        return item;
    }

    private void apply(ItemEntity item, ItemRequest request) {
        AuctionRules.require(request != null && request.getName() != null && !request.getName().isBlank(),
                HttpStatus.BAD_REQUEST, "Ten san pham khong duoc de trong.");
        String name = request.getName().trim();
        String description = request.getDescription() == null ? "" : request.getDescription().trim();
        AuctionRules.require(name.length() <= 255 && description.length() <= 4000,
                HttpStatus.BAD_REQUEST, "Ten hoac mo ta san pham qua dai.");
        item.setName(name);
        item.setDescription(description);
        item.setStartingPrice(AuctionRules.money(request.getStartingPrice()));
    }

    private ItemResponse response(ItemEntity item) {
        return new ItemResponse(item.getId(), item.getSellerId(), item.getName(), item.getDescription(), item.getStartingPrice(), item.getStatus());
    }
}
