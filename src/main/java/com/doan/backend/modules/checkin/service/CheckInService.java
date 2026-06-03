package com.doan.backend.modules.checkin.service;

import com.doan.backend.common.dto.PageResponse;
import com.doan.backend.modules.checkin.dto.request.CheckInRequest;
import com.doan.backend.modules.checkin.vo.CheckInRankingVo;
import com.doan.backend.modules.checkin.vo.CheckInVo;
import java.util.List;
import java.util.UUID;

public interface CheckInService {

    CheckInVo checkIn(CheckInRequest request);

    List<CheckInVo> findCurrentUserCheckIns();

    List<CheckInVo> findByRestaurant(UUID restaurantId);

    PageResponse<CheckInRankingVo> ranking(int page, int size);

    void delete(UUID id);
}
