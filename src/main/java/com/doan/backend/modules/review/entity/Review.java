package com.doan.backend.modules.review.entity;

import com.doan.backend.common.base.BaseEntity;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import com.doan.backend.modules.user.entity.User;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tbldanhgia")
@AttributeOverride(name = "createdAt", column = @Column(name = "ngaytao", updatable = false))
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idtaikhoan", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcuahang", nullable = false)
    private Restaurant restaurant;

    @Column(name = "danhgia", columnDefinition = "TEXT")
    private String content;

    @Column(name = "diemdanhgia", nullable = false)
    private Integer rating;

    @PrePersist
    void prePersist() {
        if (getCreatedAt() == null) {
            setCreatedAt(LocalDateTime.now());
        }
    }
}
