package vn.edu.auction.server.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.edu.auction.server.entity.ChatMessageEntity;

public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    @Query("select m from ChatMessageEntity m where (m.senderId = :a and m.receiverId = :b) "
            + "or (m.senderId = :b and m.receiverId = :a) order by m.id asc")
    List<ChatMessageEntity> conversation(@Param("a") Long a, @Param("b") Long b);
}
