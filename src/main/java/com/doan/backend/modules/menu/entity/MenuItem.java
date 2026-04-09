package com.doan.backend.modules.menu.entity;

import com.doan.backend.common.base.BaseEntity;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Getter
@Setter
@Entity
@Table(name = "mon_an")
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cua_hang_id")
    @NotFound(action = NotFoundAction.IGNORE)
    private Restaurant restaurant;

    @Column(name = "ten_mon_an", nullable = false, length = 255)
    private String name;

    @Column(name = "gia", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "huong_vi", length = 100)
    private String flavor;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Transient
    private String imageUrl;

    @Transient
    private Boolean available;
}
