package com.h_group.gvs_terceirizacoes.config;

import com.h_group.gvs_terceirizacoes.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * Emite e verifica tokens JWT assinados com HS256.
 * O prazo vem da configuração. A autorização considera o perfil atual do banco,
 * mesmo quando o token contém um perfil emitido anteriormente.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-ms}") long expirationMs
    ) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            secretBytes = sha256(secretBytes);
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Emite um token assinado para uma conta persistida.
     * @param user conta autenticada
     * @return token com identidade, perfil e prazo configurado
     */
    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("name", user.getName())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().value())
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Verifica assinatura e validade temporal antes de retornar os claims.
     * @param token JWT recebido na requisição
     * @return claims verificados
     * @throws io.jsonwebtoken.JwtException quando o token é inválido ou expirou
     * @throws IllegalArgumentException quando a entrada não é aceitável
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
