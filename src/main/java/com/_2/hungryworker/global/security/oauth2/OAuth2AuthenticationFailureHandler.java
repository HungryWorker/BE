package com._2.hungryworker.global.security.oauth2;

import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public OAuth2AuthenticationFailureHandler() {
        // 프론트의 로그인 실패 안내 페이지로 교체해서 사용하세요.
        super("/login?error=true");
    }
}
