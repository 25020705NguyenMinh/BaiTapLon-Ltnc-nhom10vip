package vn.edu.auction.shared.model;

import vn.edu.auction.shared.enums.UserRole;
import vn.edu.auction.shared.enums.UserStatus;

public class Admin extends User {

    public Admin(long id, String fullName, String email, UserStatus status) {
        super(id, fullName, email, UserRole.ADMIN, status);
    }

    public Admin() {
        super();
        setRole(UserRole.ADMIN);
    }
    
}