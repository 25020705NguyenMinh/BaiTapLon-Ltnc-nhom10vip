package vn.edu.auction.shared.model;

import java.math.BigDecimal;

import vn.edu.auction.shared.enums.ItemStatus;

public class Item {

    private Long id;
    private Long sellerId;
    private String name;
    private String description;
    private BigDecimal startingPrice;
    private ItemStatus status;

    public Item(Long id, Long sellerId, String name, String description, BigDecimal startingPrice, ItemStatus status){
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
        this.status = status;
    }

    public Item(){}

    public Long getId(){return id;}
    public Long getSellerId(){return sellerId;}
    public String getName(){return name;}
    public String getDescription(){return description;}
    public BigDecimal getStartingPrice(){return startingPrice;}
    public ItemStatus getStatus(){return status;}

    public void setId(Long newId){this.id = newId;}
    public void setSellerId(Long newSellerId){this.sellerId = newSellerId;}
    public void setName(String newName){this.name = newName;}
    public void setDescription(String newDescription){this.description = newDescription;}
    public void setStartingPrice(BigDecimal newStartingPrice){this.startingPrice = newStartingPrice;}
    public void setStatus(ItemStatus newStatus){this.status = newStatus;}
    
}
