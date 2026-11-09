package vn.edu.auction.client.controller;

// **
// * Model tạm thời mô phỏng Item (Sẽ thay thế bằng DTO/Model từ module dùng chung hoặc kết nối HTTP ở Giai đoạn 3)[cite: 3].
// */

public class ItemMock {
    private Long id;
    private String name;
    private String description;
    private Double startingPrice;

    public ItemMock(Long id, String name, String description, Double startingPrice) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startingPrice = startingPrice;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(Double startingPrice) { this.startingPrice = startingPrice; }
}
