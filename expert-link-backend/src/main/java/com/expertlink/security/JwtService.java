package com.expertlink.security;

import com.expertlink.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties jwtProperties;

    public String generateToken(long userId, String username, Set<String> roles, int tokenEpoch) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + jwtProperties.getExpiration());
        String rolesClaim = roles.stream().sorted().collect(Collectors.joining(","));
        return Jwts.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(username)
                .claim("uid", userId)
                .claim("te", tokenEpoch)
                .claim("roles", rolesClaim)
                .issuedAt(now)
                .expiration(exp)
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey signingKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : pad(keyBytes));
    }

    private static byte[] pad(byte[] src) {
        byte[] out = new byte[32];
        System.arraycopy(src, 0, out, 0, Math.min(src.length, 32));
        return out;
    }
}
