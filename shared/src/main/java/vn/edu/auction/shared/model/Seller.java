package vn.edu.auction.shared.model;

import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

public class Seller extends User {

    public Seller(Long id, String fullName, String email, UserStatus status) {
        super(id, fullName, email, UserRole.SELLER, status);
    }

    public Seller() {
        super();
        setRole(UserRole.SELLER);
    }

}