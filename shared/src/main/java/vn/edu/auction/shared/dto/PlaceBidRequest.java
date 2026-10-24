package vn.edu.auction.shared.dto;

import java.math.BigDecimal;

public class PlaceBidRequest {

    private BigDecimal amount;

    public PlaceBidRequest(BigDecimal amount){
        this.amount = amount;
    }

    public PlaceBidRequest(){}

    public BigDecimal getAmount(){return amount;}
    public void setAmount(BigDecimal newAmount){this.amount = newAmount;}
    
}
