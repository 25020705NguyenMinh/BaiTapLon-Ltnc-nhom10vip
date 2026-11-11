package vn.edu.auction.server.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import vn.edu.auction.server.entity.UserEntity;
import vn.edu.auction.server.repository.UserRepository;
import vn.edu.auction.shared.enums.*;

@Service
public class AuctionUserService {
    private final UserRepository users;

    public AuctionUserService(UserRepository users) { this.users = users; }

    public UserEntity requireRole(Long userId, UserRole role) {
        AuctionRules.require(userId != null, HttpStatus.UNAUTHORIZED, "Vui long dang nhap.");
        UserEntity user = users.findById(userId).orElse(null);
        AuctionRules.require(user != null && user.getStatus() == UserStatus.ACTIVE,
                HttpStatus.FORBIDDEN, "Tai khoan khong hoat dong.");
        AuctionRules.require(user.getRole() == role, HttpStatus.FORBIDDEN, "Tai khoan khong co quyen thuc hien.");
        return user;
    }
}
