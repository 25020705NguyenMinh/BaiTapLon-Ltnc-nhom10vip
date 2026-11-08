package vn.edu.auction.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.auction.server.entity.OtpEntity;
import vn.edu.auction.server.entity.OtpPurpose;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpEntity, Long> {

    Optional<OtpEntity> findByEmailAndPurpose(String email, OtpPurpose purpose);
}