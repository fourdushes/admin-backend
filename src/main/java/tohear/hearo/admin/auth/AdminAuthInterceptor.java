package tohear.hearo.admin.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;
import tohear.hearo.global.dto.AccountRole;
import tohear.hearo.global.exception.AuthenticationException;
import tohear.hearo.global.security.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    private final JwtTokenProvider tokenProvider;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String authorization = request.getHeader("Authorization");

        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthenticationException("관리자 인증 토큰이 필요합니다.");
        }

        String token = authorization.substring(7);

        try {
            // 만료시간, 서명, Access Token 여부 확인
            tokenProvider.validateAccessToken(token);

            // 관리자 토큰인지 확인
            if (tokenProvider.getAccountRole(token) != AccountRole.ADMIN) {
                throw new AuthenticationException("관리자만 접근할 수 있습니다.");
            }

            return true;

        } catch (AuthenticationException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuthenticationException("유효하지 않거나 만료된 관리자 토큰입니다.");
        }
    }
}
