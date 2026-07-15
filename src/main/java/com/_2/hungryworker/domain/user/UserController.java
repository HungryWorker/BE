package com._2.hungryworker.domain.user;

import com._2.hungryworker.domain.user.dto.NicknameRequest;
import com._2.hungryworker.domain.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입 API는 따로 없다. 구글 로그인(/oauth2/authorization/google) 성공 시
 * CustomOAuth2UserService가 최초 로그인이면 User를 자동 생성하기 때문이다.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public UserResponse getMe(@AuthenticationPrincipal Long userId) {
        return userService.getMe(userId);
    }

    @PatchMapping("/me/nickname")
    public UserResponse changeNickname(@AuthenticationPrincipal Long userId,
                                        @Valid @RequestBody NicknameRequest request) {
        return userService.changeNickname(userId, request.nickname());
    }
}
