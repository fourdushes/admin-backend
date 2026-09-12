package tohear.hearo.admin.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import tohear.hearo.global.dto.AccountRole;
import tohear.hearo.global.security.JwtTokenProvider;

class AdminJwtAuthenticationFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void validAdminAccessTokenCreatesRoleAdminAuthentication() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        doNothing().when(tokenProvider).validateAccessToken("admin-token");
        when(tokenProvider.getAccountRole("admin-token")).thenReturn(AccountRole.ADMIN);
        when(tokenProvider.getUserId("admin-token")).thenReturn("admin-test");

        AdminJwtAuthenticationFilter filter = new AdminJwtAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/find-care");
        request.addHeader("Authorization", "Bearer admin-token");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin-test");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
            .extracting("authority")
            .containsExactly("ROLE_ADMIN");
    }

    @Test
    void nonAdminTokenDoesNotAuthenticate() throws Exception {
        JwtTokenProvider tokenProvider = mock(JwtTokenProvider.class);
        doNothing().when(tokenProvider).validateAccessToken("user-token");
        when(tokenProvider.getAccountRole("user-token")).thenReturn(AccountRole.USER);

        AdminJwtAuthenticationFilter filter = new AdminJwtAuthenticationFilter(tokenProvider);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/admin/find-care");
        request.addHeader("Authorization", "Bearer user-token");

        filter.doFilterInternal(request, new MockHttpServletResponse(), new MockFilterChain());

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        assertThat(request.getAttribute(AdminJwtAuthenticationFilter.AUTHENTICATION_ERROR_ATTRIBUTE))
            .isEqualTo("관리자만 접근할 수 있습니다.");
    }
}
