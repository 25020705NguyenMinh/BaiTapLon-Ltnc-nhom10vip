package vn.edu.auction.server.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import vn.edu.auction.shared.enums.*;

@Entity
@Table(name = "items")
public class ItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "description", length = 4000, nullable = false)
    private String description;
    @Column(name = "starting_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal startingPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ItemStatus status;

    public ItemEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getStartingPrice() { return startingPrice; }
    public void setStartingPrice(BigDecimal startingPrice) { this.startingPrice = startingPrice; }

    public ItemStatus getStatus() { return status; }
    public void setStatus(ItemStatus status) { this.status = status; }
}
