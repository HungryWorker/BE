package com._2.hungryworker.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * BaseTimeEntity, ReviewReaction, SavedRestaurant의 @CreatedDate가
 * 실제로 채워지려면 이 설정이 반드시 필요하다.
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
