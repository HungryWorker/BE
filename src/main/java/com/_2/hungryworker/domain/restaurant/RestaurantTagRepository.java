package com._2.hungryworker.domain.restaurant;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantTagRepository extends JpaRepository<RestaurantTag, Long> {

    List<RestaurantTag> findByRestaurantId(Long restaurantId);
}