package vn.edu.auction.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.auction.server.entity.UserEntity;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}