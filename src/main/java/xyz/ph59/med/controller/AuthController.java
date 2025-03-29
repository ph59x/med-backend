package xyz.ph59.med.controller;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.entity.User;
import xyz.ph59.med.mapper.UserMapper;
import xyz.ph59.med.util.JwtUtil;
import lombok.Data;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.UUID;

@RestController
public class AuthController {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public AuthController(UserMapper userMapper, StringRedisTemplate stringRedisTemplate, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.redisTemplate = stringRedisTemplate;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Result> register(@RequestBody RegisterRequest request) {
        if (!request.getUsername().matches("^\\w{3,16}$")) {
            return ResponseEntity.badRequest().body(
                    Result.builder(HttpStatus.BAD_REQUEST)
                    .message("Invalid username format")
                    .build()
            );
        }

        User existingUser = userMapper.selectByUsername(request.getUsername());
        if (existingUser != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");
        userMapper.insertUser(user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Result.builder(HttpStatus.CREATED)
                                .message("Successfully registered.")
                                .data(new RegisterResponse(user.getId(), user.getUsername()))
                                .build()
                );
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest,
                                   HttpServletResponse response) {
        User user = userMapper.selectByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Result.builder(HttpStatus.UNAUTHORIZED)
                                    .message("Failed to match username with password.")
                                    .build()
                    );
        }

        String refreshToken = UUID.randomUUID().toString();
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(
                user.getId(),
                user.getRole(),
                getClientIp(httpRequest),
                httpRequest.getHeader("User-Agent")
        );

        redisTemplate.opsForValue().set(
                "refresh_token:" + refreshToken,
                JSON.toJSONString(tokenInfo),
                Duration.ofDays(7)
        );

        String jwt = jwtUtil.generateToken(user.getId(), user.getRole());
        Cookie cookie = new Cookie("token", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/auth/session");
        cookie.setMaxAge(604800); // 7天
        // TODO 写入cookie有效期
        response.addHeader(HttpHeaders.SET_COOKIE,
                String.format("%s=%s; Path=%s; HttpOnly; Secure; SameSite=Strict",
                        cookie.getName(), cookie.getValue(), cookie.getPath()));

        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                .body(Result.builder(HttpStatus.OK).build());
    }

    @PostMapping("/auth/session")
    public ResponseEntity<?> refreshSession(@CookieValue("token") String refreshToken,
                                            HttpServletRequest request) {
        RefreshTokenInfo tokenInfo = JSON.parseObject(redisTemplate.opsForValue().get("refresh_token:" + refreshToken), RefreshTokenInfo.class);
        if (tokenInfo == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Result.builder(HttpStatus.UNAUTHORIZED)
                                    .message("Invalid token")
                                    .build()
                    );
        }

        if (!tokenInfo.getIp().equals(getClientIp(request)) ||
                !tokenInfo.getUa().equals(request.getHeader("User-Agent"))) {
            redisTemplate.delete("refresh_token:" + refreshToken);
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(
                            Result.builder(HttpStatus.FORBIDDEN)
                                    .message("Client verification failed! Please re-login!")
                                    .build()
                    );
        }

        String newJwt = jwtUtil.generateToken(tokenInfo.getUid(), tokenInfo.getRole());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + newJwt)
                .body(Result.builder(HttpStatus.OK).build());
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

    @Data
    private static class RegisterRequest {
        private String username;
        private String password;
    }

    @Data
    private static class RegisterResponse {
        private final Integer id;
        private final String username;
    }

    @Data
    private static class LoginRequest {
        private String username;
        private String password;
    }

    @Data
    private static class RefreshTokenInfo {
        private final Integer uid;
        private final String role;
        private final String ip;
        private final String ua;
    }
}