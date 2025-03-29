package xyz.ph59.med.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtUtil {
    private static final String SECRET_FILE = "jwt-secret.key";
    private String secret;

    /**
     * JWT token过期时间
     */
    @Value("${jwt.expiration:1800}")
    private int expiration;

    @PostConstruct
    public void init() throws IOException {
        Path path = Paths.get(SECRET_FILE);
        if (Files.exists(path)) {
            secret = new String(Files.readAllBytes(path));
        } else {
            byte[] randomBytes = new byte[32];
            new SecureRandom().nextBytes(randomBytes);
            secret = Base64.getEncoder().encodeToString(randomBytes);
            Files.write(path, secret.getBytes());
        }
    }

    public String generateToken(Integer userId, String role) {
        try {
            Date expiresAt = new Date(System.currentTimeMillis() + expiration * 1000L);
            return JWT.create()
                    .withSubject(userId.toString())
                    .withClaim("role", role)
                    .withExpiresAt(expiresAt)
                    .sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException e) {
            throw new RuntimeException("Error generating JWT", e);
        }
    }

    public DecodedJWT verifyToken(String token) {
        try {
            return JWT.require(Algorithm.HMAC256(secret))
                    .build()
                    .verify(token);
        } catch (Exception e) {
            return null;
        }
    }
}