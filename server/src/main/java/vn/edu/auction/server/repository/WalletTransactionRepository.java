package vn.edu.auction.server.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import vn.edu.auction.server.entity.WalletTransactionEntity;

public interface WalletTransactionRepository extends JpaRepository<WalletTransactionEntity, Long> {
    List<WalletTransactionEntity> findTop100ByUserIdOrderByIdDesc(Long userId);
}
