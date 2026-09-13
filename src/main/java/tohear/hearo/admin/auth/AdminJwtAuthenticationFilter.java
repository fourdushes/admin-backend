package tohear.hearo.admin.auth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import tohear.hearo.global.dto.AccountRole;
import tohear.hearo.global.security.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class AdminJwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHENTICATION_ERROR_ATTRIBUTE =
        AdminJwtAuthenticationFilter.class.getName() + ".error";

    private final JwtTokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization != null && authorization.startsWith("Bearer ")) {
            authenticate(request, authorization.substring(7));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request, String token) {
        try {
            tokenProvider.validateAccessToken(token);

            if (tokenProvider.getAccountRole(token) != AccountRole.ADMIN) {
                request.setAttribute(AUTHENTICATION_ERROR_ATTRIBUTE, "관리자만 접근할 수 있습니다.");
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    tokenProvider.getUserId(token),
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            request.setAttribute(
                AUTHENTICATION_ERROR_ATTRIBUTE,
                "유효하지 않거나 만료된 관리자 토큰입니다.");
        }
    }
}
