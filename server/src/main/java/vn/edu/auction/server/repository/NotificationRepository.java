package vn.edu.auction.server.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.NotificationEntity;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    List<NotificationEntity> findTop50ByUserIdOrderByIdDesc(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("update NotificationEntity n set n.seen = true where n.userId = :uid and n.seen = false")
    void markAllSeen(@Param("uid") Long userId);
}
