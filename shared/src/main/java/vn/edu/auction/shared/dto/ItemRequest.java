package vn.edu.auction.shared.dto;

import java.math.BigDecimal;

public class ItemRequest {

    private String name;
    private String description;
    private BigDecimal startingPrice;

    public ItemRequest(String name, String description, BigDecimal startingPrice){
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public ItemRequest(){}

    public String getName(){return name;}
    public String getDescription(){return description;}
    public BigDecimal getStartingPrice(){return startingPrice;}

    public void setName(String newName){this.name = newName;}
    public void setDescription(String newDescription){this.description = newDescription;}
    public void setStartingPrice(BigDecimal newStartingPrice){this.startingPrice = newStartingPrice;}

}
