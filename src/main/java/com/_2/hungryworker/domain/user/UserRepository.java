package com._2.hungryworker.domain.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByGoogleId(String googleId);

    boolean existsByGoogleId(String googleId);

    boolean existsByNickname(String nickname);
}
