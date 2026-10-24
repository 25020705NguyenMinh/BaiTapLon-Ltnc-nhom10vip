package vn.edu.auction.shared.model;

import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

public class Bidder extends User {

    public Bidder(Long id, String fullName, String email, UserStatus status) {
        super(id, fullName, email, UserRole.BIDDER, status);
    }

    public Bidder() {
        super();
        setRole(UserRole.BIDDER);
    }

}