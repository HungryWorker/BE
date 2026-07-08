package com._2.hungryworker.domain.user;

import com._2.hungryworker.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "users",
    uniqueConstraints = @UniqueConstraint(name = "uk_user_google_id", columnNames = "google_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    // 구글 OAuth2 sub 값
    @Column(name = "google_id", nullable = false, length = 100)
    private String googleId;

    @Builder
    public User(String nickname, String googleId) {
        this.nickname = nickname;
        this.googleId = googleId;
    }

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }
}
