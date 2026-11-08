package vn.edu.auction.server.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import vn.edu.auction.server.service.ItemService;
import vn.edu.auction.shared.dto.*;

@RestController
@RequestMapping("/api/items")
public class ItemController {
    private final ItemService items;

    public ItemController(ItemService items) { this.items = items; }

    @GetMapping
    public List<ItemResponse> getAllItems() { return items.getAllItems(); }

    @GetMapping("/{id}")
    public ItemResponse getItemById(@PathVariable("id") Long id) { return items.getItemById(id); }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemResponse createItem(@RequestBody ItemRequest request, Authentication auth) {
        return items.createItem(request, AuctionPrincipal.userId(auth));
    }

    @PutMapping("/{id}")
    public ItemResponse updateItem(@PathVariable("id") Long id, @RequestBody ItemRequest request, Authentication auth) {
        return items.updateItem(id, request, AuctionPrincipal.userId(auth));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteItem(@PathVariable("id") Long id, Authentication auth) { items.deleteItem(id, AuctionPrincipal.userId(auth)); }
}
