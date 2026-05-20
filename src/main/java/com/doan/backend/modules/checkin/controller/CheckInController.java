package com.doan.backend.modules.checkin.controller;

import com.doan.backend.common.dto.ApiResponse;
import com.doan.backend.modules.checkin.dto.request.CheckInRequest;
import com.doan.backend.modules.checkin.service.CheckInService;
import com.doan.backend.modules.checkin.vo.CheckInVo;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/check-ins")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping
    public ApiResponse<CheckInVo> checkIn(@Valid @RequestBody CheckInRequest request) {
        CheckInVo result = checkInService.checkIn(request);
        String message = result.getThanhCong() ? "Check-in thành công" : "Check-in không thành công";
        return ApiResponse.success(message, result);
    }

    @GetMapping("/me")
    public ApiResponse<List<CheckInVo>> findCurrentUserCheckIns() {
        return ApiResponse.success(checkInService.findCurrentUserCheckIns());
    }

    @GetMapping("/restaurants/{restaurantId}")
    public ApiResponse<List<CheckInVo>> findByRestaurant(@PathVariable UUID restaurantId) {
        return ApiResponse.success(checkInService.findByRestaurant(restaurantId));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable UUID id) {
        checkInService.delete(id);
        return ApiResponse.success("Xóa check-in thành công", "Xóa check-in thành công");
    }
}
