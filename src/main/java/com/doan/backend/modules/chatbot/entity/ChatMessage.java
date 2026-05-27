package com.doan.backend.modules.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@Entity
@Table(name = "tbl_tin_nhan_chat")
public class ChatMessage {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phien_chat")
    private ChatSession session;

    @Column(name = "vai_tro", length = 20)
    private String role;

    @Column(name = "noi_dung", columnDefinition = "TEXT")
    private String content;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "du_lieu_json", columnDefinition = "jsonb")
    private String jsonData;

    @Column(name = "so_token")
    private Integer tokenCount;

    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (tokenCount == null) {
            tokenCount = 0;
        }
    }
}
