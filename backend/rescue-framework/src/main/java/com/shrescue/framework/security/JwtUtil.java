package com.shrescue.framework.security;

import com.shrescue.common.constant.Constants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * JWT 工具（HS256）
 */
@Component
public class JwtUtil {

    @Value("${shrescue.jwt.secret}")
    private String secret;

    @Value("${shrescue.jwt.expire-seconds}")
    private long expireSeconds;

    private SecretKey key() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(LoginUser user) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expireSeconds * 1000L);
        return Jwts.builder()
                .subject(String.valueOf(user.getUserId()))
                .claim(Constants.CLAIM_USERNAME, user.getUsername())
                .claim(Constants.CLAIM_REAL_NAME, user.getRealName())
                .claim(Constants.CLAIM_LEVEL, user.getLevel())
                .claim(Constants.CLAIM_DEPT_ID, user.getDeptId())
                .claim(Constants.CLAIM_ROLES, user.getRoles())
                .claim(Constants.CLAIM_DATA_SCOPE, user.getDataScope())
                .issuedAt(now)
                .expiration(exp)
                .signWith(key())
                .compact();
    }

    public LoginUser parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key()).build()
                .parseSignedClaims(token).getPayload();
        LoginUser u = new LoginUser();
        u.setUserId(Long.valueOf(claims.getSubject()));
        u.setUsername(claims.get(Constants.CLAIM_USERNAME, String.class));
        u.setRealName(claims.get(Constants.CLAIM_REAL_NAME, String.class));
        u.setLevel(toInt(claims.get(Constants.CLAIM_LEVEL, Number.class)));
        u.setDeptId(toLong(claims.get(Constants.CLAIM_DEPT_ID, Number.class)));
        u.setRoles(claims.get(Constants.CLAIM_ROLES, List.class));
        u.setDataScope(toInt(claims.get(Constants.CLAIM_DATA_SCOPE, Number.class)));
        return u;
    }

    private Long toLong(Number n) {
        return n == null ? null : n.longValue();
    }

    private Integer toInt(Number n) {
        return n == null ? null : n.intValue();
    }
}
