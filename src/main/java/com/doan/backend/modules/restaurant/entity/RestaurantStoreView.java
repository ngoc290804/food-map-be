package com.doan.backend.modules.restaurant.entity;

import com.doan.backend.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cua_hang")
public class RestaurantStoreView extends BaseEntity {

    @Column(name = "ten_quan_an", nullable = false, length = 255)
    private String name;

    @Column(name = "dia_chi", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "gio_mo_cua")
    private LocalTime openTime;

    @Column(name = "gio_dong_cua")
    private LocalTime closeTime;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String description;

    @Transient
    private String imageUrl;

    @Transient
    private String status;
}
