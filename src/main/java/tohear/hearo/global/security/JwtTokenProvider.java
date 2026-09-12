package tohear.hearo.global.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import tohear.hearo.global.dto.AccountRole;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.admin-validity-in-milliseconds:7200000}")
    private long adminAccessTokenValidityInMilliseconds;

    @Value("${jwt.admin-refresh-token-validity-in-milliseconds:43200000}")
    private long adminRefreshTokenValidityInMilliseconds;

    public String createAccessToken(String adminId) {
        return createToken(adminId, "ACCESS", adminAccessTokenValidityInMilliseconds);
    }

    public String createRefreshToken(String adminId) {
        return createToken(adminId, "REFRESH", adminRefreshTokenValidityInMilliseconds);
    }

    public String getUserId(String token) {
        return getClaims(token).getSubject();
    }

    public AccountRole getAccountRole(String token) {
        String role = getClaims(token).get("role", String.class);
        return AccountRole.valueOf(role);
    }

    public void validateAccessToken(String token) {
        String tokenType = getClaims(token).get("tokenType", String.class);
        if (!"ACCESS".equals(tokenType)) {
            throw new IllegalArgumentException("Access Token이 아닙니다.");
        }
    }

    public long getAdminRefreshTokenValidityInMilliseconds() {
        return adminRefreshTokenValidityInMilliseconds;
    }

    private String createToken(String adminId, String tokenType, long validityInMilliseconds) {
        Claims claims = Jwts.claims().setSubject(adminId);
        claims.put("role", AccountRole.ADMIN.name());
        claims.put("tokenType", tokenType);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
