package xyz.ph59.med.service;

import com.alibaba.fastjson2.JSON;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import xyz.ph59.med.entity.LoginResponseInfo;
import xyz.ph59.med.entity.RefreshTokenInfo;
import xyz.ph59.med.entity.response.RegisterResponse;
import xyz.ph59.med.entity.User;
import xyz.ph59.med.exception.InvalidTokenException;
import xyz.ph59.med.exception.UnauthorizedException;
import xyz.ph59.med.exception.VerificationFailException;
import xyz.ph59.med.mapper.UserMapper;
import xyz.ph59.med.util.JwtUtil;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, StringRedisTemplate redisTemplate, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.redisTemplate = redisTemplate;
        this.jwtUtil = jwtUtil;
    }

    public RegisterResponse register(String username, String password) {
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        user.setHash(passwordEncoder.encode(password));
        user.setRole("USER");
        userMapper.insertUser(user);

        return new RegisterResponse(user.getId(),  user.getUsername());
    }

    public LoginResponseInfo login(String username, String password, String ip, String ua) throws UnauthorizedException {
        User user = userMapper.selectByUsername(username);
        if (user == null || !passwordEncoder.matches(password,  user.getHash()))  {
            throw new UnauthorizedException("Failed to match username with password.");
        }
        String refreshToken = UUID.randomUUID().toString();
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(
                user.getId(),
                user.getRole(),
                ip,
                ua
        );

        redisTemplate.opsForValue().set(
                "refresh_token:" + refreshToken,
                JSON.toJSONString(tokenInfo),
                Duration.ofDays(7)
        );

        String jwt = jwtUtil.generateToken(user.getId(), user.getRole());

        return new LoginResponseInfo(jwt, refreshToken);
    }

    public String refreshSession(String refreshToken, String ip, String ua) throws InvalidTokenException, VerificationFailException {
        RefreshTokenInfo tokenInfo = JSON.parseObject(redisTemplate.opsForValue().get("refresh_token:" + refreshToken), RefreshTokenInfo.class);
        if (tokenInfo == null) {
            throw new InvalidTokenException();
        }

        if (!tokenInfo.getIp().equals(ip) ||
                !tokenInfo.getUa().equals(ua)) {
            redisTemplate.delete("refresh_token:" + refreshToken);
            throw new VerificationFailException();
        }

        return jwtUtil.generateToken(tokenInfo.getUid(), tokenInfo.getRole());
    }
}
