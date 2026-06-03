package com.doan.backend.modules.checkin.repository;

import com.doan.backend.modules.checkin.entity.CheckIn;
import com.doan.backend.modules.checkin.vo.CheckInRankingVo;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CheckInRepository extends JpaRepository<CheckIn, UUID> {

    List<CheckIn> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<CheckIn> findByIdAndUserId(UUID id, UUID userId);

    List<CheckIn> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    @Query(
            value = """
                    select new com.doan.backend.modules.checkin.vo.CheckInRankingVo(
                        c.user.username,
                        c.user.fullName,
                        count(c)
                    )
                    from CheckIn c
                    where c.checkIn = 1
                    group by c.user.id, c.user.username, c.user.fullName
                    order by count(c) desc, c.user.username asc
                    """,
            countQuery = """
                    select count(distinct c.user.id)
                    from CheckIn c
                    where c.checkIn = 1
                    """
    )
    Page<CheckInRankingVo> findSuccessfulCheckInRanking(Pageable pageable);
}
