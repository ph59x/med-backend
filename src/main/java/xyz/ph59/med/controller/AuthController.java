package xyz.ph59.med.controller;

import lombok.RequiredArgsConstructor;
import xyz.ph59.med.dto.LoginResponseInfo;
import xyz.ph59.med.dto.request.LoginRequest;
import xyz.ph59.med.dto.request.RegisterRequest;
import xyz.ph59.med.dto.response.RegisterResponse;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.exception.InvalidTokenException;
import xyz.ph59.med.exception.UnauthorizedException;
import xyz.ph59.med.exception.VerificationFailException;
import xyz.ph59.med.service.AuthService;
import xyz.ph59.med.util.IpUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/auth/register")
    public ResponseEntity<Result> register(@RequestBody RegisterRequest request) {
        if (!request.getUsername().matches("^\\w{3,16}$")) {
            return ResponseEntity.badRequest().body(
                    Result.builder(HttpStatus.BAD_REQUEST)
                    .message("Invalid username format")
                    .build()
            );
        }

        RegisterResponse registerResponse = authService.register(request.getUsername(), request.getPassword());
        // TODO 注册失败处理
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.builder(HttpStatus.CREATED)
                        .message("Successfully registered.")
                        .data(registerResponse)
                        .build()
                );
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Result> login(@RequestBody LoginRequest request,
                                   HttpServletRequest httpRequest) {
        // TODO 布隆过滤器筛选有效用户

        LoginResponseInfo info;
        try {
            String ip = IpUtil.getClientIp(httpRequest);
            String ua = httpRequest.getHeader("User-Agent");

            info = authService.login(request.getUsername(), request.getPassword(), ip, ua);
        }
        catch (UnauthorizedException e) {
            // TODO 日志记录
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.builder(HttpStatus.UNAUTHORIZED)
                            .message(e.getMessage())
                            .build()
                    );
        }

        return ResponseEntity.ok()
                .header("X-Refresh-Token", info.getRefreshToken())
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + info.getAccessToken())
                .body(Result.builder(HttpStatus.OK)
                        .data(info)
                        .build());
    }

    @PostMapping("/auth/session")
    public ResponseEntity<?> refreshSession(@CookieValue("token") String refreshToken,
                                            HttpServletRequest request) {
        String ip = IpUtil.getClientIp(request);
        String ua = request.getHeader("User-Agent");

        try {
            String newJwt = authService.refreshSession(refreshToken, ip, ua);
            return ResponseEntity.noContent()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + newJwt)
                    .build();
        } catch (InvalidTokenException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.builder(HttpStatus.UNAUTHORIZED)
                            .message("Invalid token")
                            .build()
                    );
        } catch (VerificationFailException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Result.builder(HttpStatus.FORBIDDEN)
                            .message("Client verification failed! Please re-login!")
                            .build()
                    );
        }
    }
}