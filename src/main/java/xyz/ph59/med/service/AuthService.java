package xyz.ph59.med.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import xyz.ph59.med.dto.LoginResponseInfo;
import xyz.ph59.med.dto.RefreshTokenInfo;
import xyz.ph59.med.dto.response.RegisterResponse;
import xyz.ph59.med.entity.User;
import xyz.ph59.med.exception.InvalidTokenException;
import xyz.ph59.med.exception.UnauthorizedException;
import xyz.ph59.med.exception.VerificationFailException;
import xyz.ph59.med.mapper.UserMapper;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final StringRedisTemplate redisTemplate;
    private final PermissionService permissionService;

    public RegisterResponse register(String username, String password) {
        User existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            return null;
        }

        User user = new User();
        user.setUsername(username);
        user.setHash(BCrypt.hashpw(password, BCrypt.gensalt(12)));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insertUser(user);

        return new RegisterResponse(user.getId(),  user.getUsername());
    }

    public LoginResponseInfo login(String username, String password, String ip, String ua) throws UnauthorizedException {
        User user = userMapper.selectByUsername(username);
        if (user == null || !BCrypt.checkpw(password, user.getHash())) {
            throw new UnauthorizedException("Failed to match username with password.");
        }

        String refreshToken = UUID.randomUUID().toString();
        RefreshTokenInfo tokenInfo = new RefreshTokenInfo(
                user.getId(),
                ip,
                ua
        );
        redisTemplate.opsForValue().set(
                "refresh_token:" + refreshToken,
                JSON.toJSONString(tokenInfo),
                Duration.ofDays(7)
        );

        // TODO 异步缓存用户权限
        permissionService.getUserPermission(user.getId());

        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();

        return new LoginResponseInfo(accessToken, refreshToken);
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

        StpUtil.login(tokenInfo.getUid());
        // TODO 异步刷新用户权限
        permissionService.refreshUserPermission(tokenInfo.getUid());

        return StpUtil.getTokenValue();
    }
}
