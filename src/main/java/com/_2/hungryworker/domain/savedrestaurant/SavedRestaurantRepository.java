package com._2.hungryworker.domain.savedrestaurant;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedRestaurantRepository extends JpaRepository<SavedRestaurant, Long> {

    List<SavedRestaurant> findByUserId(Long userId);

    Optional<SavedRestaurant> findByUserIdAndRestaurantId(Long userId, Long restaurantId);

    boolean existsByUserIdAndRestaurantId(Long userId, Long restaurantId);

    void deleteByUserIdAndRestaurantId(Long userId, Long restaurantId);
}
