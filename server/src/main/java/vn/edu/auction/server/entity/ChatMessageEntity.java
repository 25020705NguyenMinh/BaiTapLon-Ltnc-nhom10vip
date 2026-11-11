package vn.edu.auction.server.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessageEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) public Long id;
    @Column(name = "sender_id", nullable = false) public Long senderId;
    @Column(name = "receiver_id", nullable = false) public Long receiverId;
    @Column(nullable = false, length = 1000) public String content;
    @Column(name = "sent_at", nullable = false) public LocalDateTime sentAt;
}
