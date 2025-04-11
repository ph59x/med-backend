package xyz.ph59.med.aspect;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import xyz.ph59.med.annotation.CheckScope;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.entity.permission.UserPermission;
import xyz.ph59.med.service.EvalService;
import xyz.ph59.med.service.PermissionService;
import xyz.ph59.med.service.RelationService;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Aspect
@Component
@RequiredArgsConstructor
public class ScopeCheckAspect {
    private final PermissionService permissionService;
    private final EvalService evalService;
    private final RelationService relationService;

    // Around与Before的区别
    @Around("@annotation(xyz.ph59.med.annotation.CheckScope)")
    public Object handleScopeChcek(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取方法签名和注解实例
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        CheckScope annotation = method.getAnnotation(CheckScope.class);

        int id = StpUtil.getLoginIdAsInt();

        Integer targetId = null;
        try {
            Object arg = Objects.requireNonNull(joinPoint.getArgs()[0]);
            if (arg instanceof Integer) {
                /*
                  直接获取targetId
                  例如 POST /eval
                 */
                targetId = (Integer) arg;
            }
            else if (arg instanceof String) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

                if (request.getMethod().equals(HttpMethod.GET.name())
                        && request.getServletPath().equals("/eval")) {
                    /*
                      从任务中查询targetId
                      仅在请求为 GET /eval 时执行
                     */
                    UUID.fromString((String) arg);
                    targetId = evalService.queryTargetId((String) arg);
                }
            }
        }
        catch (NullPointerException ignored) {}
        catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Result.builder(HttpStatus.BAD_REQUEST)
                            .message("无效的任务id")
                            .build()
                    );
        }
        /*
          如果targetId仍为null
          说明目标方法与该参数无关
         */
        if (targetId == null) {
            return joinPoint.proceed();
        }

        boolean isSameUser = id == targetId;

        UserPermission permission = permissionService.getUserPermission(id);
        Set<String> strings = permission.getPermissions().get(annotation.value());

        for (String s : strings) {
            switch (s) {
                case "SELF":
                    if (isSameUser) {
                        return joinPoint.proceed();
                    }
                    else {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Result.builder(HttpStatus.FORBIDDEN)
                                        .message("你没有权限操作目标账户")
                                        .build()
                                );
                    }
                case "RELATED_USERS":
                    if (!relationService.getUserRelation(id).contains(targetId)) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Result.builder(HttpStatus.FORBIDDEN)
                                        .message("你没有权限操作目标账户")
                                        .build()
                                );
                    }
                    return joinPoint.proceed();
                case "DEPARTMENT":
                    // TODO 部门管理员的作用域校验
            }
        }
        return joinPoint.proceed();
    }
}
