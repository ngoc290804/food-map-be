package com.doan.backend.modules.menu.entity;

import com.doan.backend.common.base.BaseEntity;
import com.doan.backend.modules.restaurant.entity.Restaurant;
import jakarta.persistence.AttributeOverride;
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
@Table(name = "tblmenu")
@AttributeOverride(name = "createdAt", column = @Column(name = "ngaytao", updatable = false))
public class MenuItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idcuahang")
    @NotFound(action = NotFoundAction.IGNORE)
    private Restaurant restaurant;

    @Column(name = "tenmonan", nullable = false, length = 200)
    private String name;

    @Column(name = "giatien", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "nguyenlieuchinh", columnDefinition = "TEXT")
    private String flavor;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String description;

    @Transient
    private String imageUrl;

    @Transient
    private Boolean available;
}
