package com.doan.backend.modules.checkin.service;

import com.doan.backend.modules.checkin.dto.request.CheckInRequest;
import com.doan.backend.modules.checkin.vo.CheckInVo;
import java.util.List;
import java.util.UUID;

public interface CheckInService {

    CheckInVo checkIn(CheckInRequest request);

    List<CheckInVo> findCurrentUserCheckIns();

    List<CheckInVo> findByRestaurant(UUID restaurantId);

    void delete(UUID id);
}
