package com.doan.backend.modules.restaurant.repository;

import com.doan.backend.modules.restaurant.entity.Restaurant;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID>, JpaSpecificationExecutor<Restaurant> {

    @Query("select r from Restaurant r where r.id = :id and coalesce(r.danhDauXoa, 0) = 0")
    Optional<Restaurant> findActiveById(@Param("id") UUID id);

    Optional<Restaurant> findByIdChuCuaHang(UUID idChuCuaHang);

    @Query("select count(r) > 0 from Restaurant r where r.id = :id and coalesce(r.danhDauXoa, 0) = 0")
    boolean existsActiveById(@Param("id") UUID id);

    @Modifying
    @Query(value = """
            update tblquanan
            set diemdanhgia = (
                select round(coalesce(avg(diemdanhgia), 0)::numeric, 1)
                from tbldanhgia
                where idcuahang = :id
            )
            where id = :id
            """, nativeQuery = true)
    void refreshRating(@Param("id") UUID id);
}
