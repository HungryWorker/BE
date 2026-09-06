package com._2.hungryworker.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * "Authorization: Bearer {token}" 헤더를 검사해서 유효하면 SecurityContext에
 * 인증 정보를 채워 넣는다. 인증 주체(principal)는 User PK(Long userId)로 둔다.
 * 컨트롤러에서는 @AuthenticationPrincipal Long userId 로 바로 꺼내 쓸 수 있다.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);

        System.out.println("===== JWT REQUEST =====");
        System.out.println("method = " + request.getMethod());
        System.out.println("uri = " + request.getRequestURI());
        System.out.println("token exists = " + (token != null));

        if (token != null) {
            System.out.println("===== JWT FILTER =====");
            System.out.println("token exists = true");
            System.out.println("token valid = " + jwtTokenProvider.validateToken(token));

            if (jwtTokenProvider.validateToken(token)) {
                Long userId = jwtTokenProvider.getUserId(token);
                System.out.println("userId = " + userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, List.of());

                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authentication);

                System.out.println("authenticated = "
                        + SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HEADER);
        if (bearer != null && bearer.startsWith(PREFIX)) {
            return bearer.substring(PREFIX.length());
        }
        return null;
    }
}
