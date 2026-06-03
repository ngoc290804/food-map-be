package com.doan.backend.modules.restaurant.entity;

import com.doan.backend.common.base.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "tblquanan")
@AttributeOverride(name = "createdAt", column = @Column(name = "ngaytao", updatable = false))
public class Restaurant extends BaseEntity {

    @Column(name = "tenquanan", nullable = false, length = 200)
    private String name;

    @Column(name = "diachi", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "giomocua")
    private LocalTime openTime;

    @Column(name = "giodongcua")
    private LocalTime closeTime;

    @Column(name = "mota", columnDefinition = "TEXT")
    private String description;

    @Column(name = "loaicuahang", length = 100)
    private String loaiCuaHang;

    @Column(name = "loaikinhdoanh", length = 100)
    private String loaiKinhDoanh;

    @Column(name = "hinhanh", length = 1000)
    private String imageUrl;

    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "diemdanhgia", precision = 3, scale = 1)
    private BigDecimal diemDanhGia;

    @Column(name = "idchucuahang")
    private UUID idChuCuaHang;

    @Column(name = "danhdauxoa")
    private Integer danhDauXoa;

    @Transient
    private String status;
}
