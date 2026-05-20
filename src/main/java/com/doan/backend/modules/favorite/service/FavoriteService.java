package com.doan.backend.modules.favorite.service;

import com.doan.backend.modules.favorite.dto.request.FavoriteRequest;
import com.doan.backend.modules.favorite.vo.FavoriteVo;
import java.util.List;
import java.util.UUID;

public interface FavoriteService {

    List<FavoriteVo> findCurrentUserFavorites();

    FavoriteVo create(FavoriteRequest request);

    FavoriteVo update(UUID id, FavoriteRequest request);

    void delete(UUID id);

    void deleteByRestaurant(UUID restaurantId);
}
