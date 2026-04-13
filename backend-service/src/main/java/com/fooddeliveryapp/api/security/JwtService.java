package com.fooddeliveryapp.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fooddeliveryapp.api.models.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;
    private final String issuer;
    private final long expiresSeconds;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.issuer}") String issuer,
            @Value("${app.jwt.expiresMinutes}") long expiresMinutes
    ) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.issuer = issuer;
        this.expiresSeconds = Math.max(60, expiresMinutes * 60);
        this.verifier = JWT.require(algorithm).withIssuer(issuer).build();
    }

    public String issueToken(Long userId, Role role) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(issuer)
                .withSubject(String.valueOf(userId))
                .withClaim("role", role != null ? role.name() : Role.CUSTOMER.name())
                .withIssuedAt(now)
                .withExpiresAt(now.plusSeconds(expiresSeconds))
                .sign(algorithm);
    }

    public JwtPrincipal verifyAndParse(String token) throws JWTVerificationException {
        DecodedJWT jwt = verifier.verify(token);
        Long userId = Long.parseLong(jwt.getSubject());
        String roleStr = jwt.getClaim("role").asString();
        Role role = roleStr != null ? Role.valueOf(roleStr) : Role.CUSTOMER;
        return new JwtPrincipal(userId, role);
    }
}

