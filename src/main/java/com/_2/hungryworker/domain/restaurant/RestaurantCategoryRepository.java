package com._2.hungryworker.domain.restaurant;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantCategoryRepository extends JpaRepository<RestaurantCategory, Long> {

    List<RestaurantCategory> findByRestaurantId(Long restaurantId);
}
