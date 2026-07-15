package com._2.hungryworker.global.security.oauth2;

import com._2.hungryworker.domain.user.NicknameGenerator;
import com._2.hungryworker.domain.user.User;
import com._2.hungryworker.domain.user.UserRepository;
import java.util.Map;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 구글 로그인 성공 시 Spring Security가 호출한다.
 * 구글 sub(고유 id) 기준으로 User가 있으면 조회, 없으면 새로 생성한다.
 * 즉, 회원가입은 별도 API 없이 "구글 로그인 = 회원가입"으로 동작한다.
 * 신규 가입자의 닉네임은 구글 실명이 아니라 "배고픈{동물}#{4자리}" 형식으로 자동 생성한다.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final NicknameGenerator nicknameGenerator;

    public CustomOAuth2UserService(UserRepository userRepository, NicknameGenerator nicknameGenerator) {
        this.userRepository = userRepository;
        this.nicknameGenerator = nicknameGenerator;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String googleId = (String) attributes.get("sub");

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .googleId(googleId)
                                .nickname(nicknameGenerator.generate())
                                .build()
                ));

        return new OAuth2UserPrincipal(user, attributes);
    }
}
