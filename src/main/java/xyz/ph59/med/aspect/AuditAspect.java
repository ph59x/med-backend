package xyz.ph59.med.aspect;

import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.ph59.med.entity.OperationLog;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.entity.request.LoginRequest;
import xyz.ph59.med.service.LogService;
import xyz.ph59.med.util.IpUtil;
import xyz.ph59.med.util.JwtUtil;

@Aspect
@Component
public class AuditAspect {
    private LogService logService;
    private JwtUtil jwtUtil;

    public AuditAspect(LogService logService, JwtUtil jwtUtil) {
        this.logService = logService;
        this.jwtUtil = jwtUtil;
    }

    @Around("execution(public * xyz.ph59.med.controller.AuthController.login(..))")
    public Object loginAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog log = new OperationLog();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.setIp(IpUtil.getClientIp(request));
        log.setUa(request.getHeader("User-Agent"));
        log.setRequestMethod(request.getMethod());
        log.setPath(request.getServletPath());

        LoginRequest loginRequest = (LoginRequest) joinPoint.getArgs()[0];

        log.setUsername(loginRequest.getUsername());

        log.setEventType("AUTH");
        log.setAction("LOGIN");


        ResponseEntity<?> result = (ResponseEntity<?>) joinPoint.proceed();

        switch (result.getStatusCode().value()) {
            case 200:
                log.setStatus(200);
                log.setResult("SUCCESS");
                break;
            case 401:
                log.setStatus(401);
                log.setResult("UNAUTHORIZED");
                if (result.hasBody()) {
                    Result tmp = (Result) result.getBody();
                    log.setErrorMessage(tmp.getMessage());
                }
                break;
        }

        logService.saveOperationLog(log);

        return result;
    }

    @Around("execution(public * xyz.ph59.med.controller.AuthController.refreshSession(..))")
    public Object sessionAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog log = new OperationLog();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.setIp(IpUtil.getClientIp(request));
        log.setUa(request.getHeader("User-Agent"));
        log.setRequestMethod(request.getMethod());
        log.setPath(request.getServletPath());

        log.setEventType("AUTH");
        log.setAction("REFRESH_SESSION");


        ResponseEntity<?> result = (ResponseEntity<?>) joinPoint.proceed();

        switch (result.getStatusCode().value()) {
            case 200:
                log.setStatus(200);
                log.setResult("SUCCESS");

                DecodedJWT jwt = jwtUtil.verifyToken(result.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0).substring(7));
                log.setUserId(Long.valueOf(jwt.getSubject()));

                break;
            case 401:
                log.setStatus(401);
                log.setResult("UNAUTHORIZED");
                if (result.hasBody()) {
                    Result tmp = (Result) result.getBody();
                    log.setErrorMessage(tmp.getMessage());
                }
                break;
            case 403:
                log.setStatus(403);
                log.setResult("REJECTED");
                if (result.hasBody()) {
                    Result tmp = (Result) result.getBody();
                    log.setErrorMessage(tmp.getMessage());
                }
                break;
        }

        logService.saveOperationLog(log);

        return result;
    }

    @Around("execution(public * xyz.ph59.med.controller.DataController.writeData(..))")
    public Object dataUploadAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        OperationLog log = new OperationLog();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        log.setIp(IpUtil.getClientIp(request));
        log.setUa(request.getHeader("User-Agent"));
        log.setRequestMethod(request.getMethod());
        log.setPath(request.getServletPath());

        log.setEventType("DATA");
        log.setAction("WRITE_DATA");

        log.setUserId((Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal());

        ResponseEntity<?> result = (ResponseEntity<?>) joinPoint.proceed();

        switch (result.getStatusCode().value()) {
            case 202:
                log.setStatus(202);
                log.setResult("SUCCESS");
                break;
            case 401:
                log.setStatus(400);
                log.setResult("INVALID_FORMAT");
                if (result.hasBody()) {
                    Result tmp = (Result) result.getBody();
                    log.setErrorMessage(tmp.getMessage());
                }
                break;
        }

        logService.saveOperationLog(log);

        return result;
    }
}
