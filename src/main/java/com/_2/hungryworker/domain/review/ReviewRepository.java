package com._2.hungryworker.domain.review;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId);

    List<Review> findByUserIdOrderByCreatedAtDesc(Long userId);

    long countByRestaurantId(Long restaurantId);

    @Query("select avg(r.rating) from Review r where r.restaurant.id = :restaurantId")
    Optional<Double> findAverageRatingByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("select avg(r.tasteScore) from Review r where r.restaurant.id = :restaurantId")
    Optional<Double> findAverageTasteScoreByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("select avg(r.portionScore) from Review r where r.restaurant.id = :restaurantId")
    Optional<Double> findAveragePortionScoreByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("select avg(r.valueScore) from Review r where r.restaurant.id = :restaurantId")
    Optional<Double> findAverageValueScoreByRestaurantId(@Param("restaurantId") Long restaurantId);

    @Query("select avg(r.hygieneScore) from Review r where r.restaurant.id = :restaurantId")
    Optional<Double> findAverageHygieneScoreByRestaurantId(@Param("restaurantId") Long restaurantId);
}