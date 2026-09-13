package tohear.hearo.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import tohear.hearo.global.dto.AccountRole;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(
            tokenProvider,
            "secretKey",
            "test-only-admin-jwt-secret-key-with-more-than-32-bytes");
        ReflectionTestUtils.setField(tokenProvider, "adminAccessTokenValidityInMilliseconds", 60_000L);
        ReflectionTestUtils.setField(tokenProvider, "adminRefreshTokenValidityInMilliseconds", 120_000L);
    }

    @Test
    void accessTokenContainsAdminRoleAndAccessType() {
        String token = tokenProvider.createAccessToken("admin-test");

        tokenProvider.validateAccessToken(token);
        assertThat(tokenProvider.getUserId(token)).isEqualTo("admin-test");
        assertThat(tokenProvider.getAccountRole(token)).isEqualTo(AccountRole.ADMIN);
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String token = tokenProvider.createRefreshToken("admin-test");

        assertThatThrownBy(() -> tokenProvider.validateAccessToken(token))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Access Token이 아닙니다.");
    }
}
