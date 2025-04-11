package xyz.ph59.med.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ph59.med.annotation.CheckScope;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.service.EvalService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@RestController
@RequestMapping("/eval")
@RequiredArgsConstructor
public class EvalController {
    private final EvalService evalService;

    @SaCheckPermission("TASK_CREATE")
    @SaCheckRole(value = {"USER", "DOCTOR", "DEPT_ADMIN"}, mode = SaMode.OR)
    @CheckScope("TASK_CREATE")
    @PostMapping
    public ResponseEntity<Result> createEvalTask(
            @RequestParam(value = "uid", required = false) Integer uid,
            @RequestParam("start") String startTimeStr,
            @RequestParam("end") String endTimeStr
    ) {

        ZonedDateTime start, end;
        try {
            start = ZonedDateTime.parse(startTimeStr);
            end = ZonedDateTime.parse(endTimeStr);
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest().body(
                    Result.builder(HttpStatus.BAD_REQUEST)
                            .message("Invalid time format. Should be RFC 3339 style.")
                            .build()
            );
        }

        if (uid == null) {
            uid = StpUtil.getLoginIdAsInt();
        }
        // TODO 指定UID与请求发起者不同时的权限检查

        try {
            Object result = evalService.createTask(uid, start, end);


            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Result.builder(HttpStatus.OK)
                            .message("任务已创建")
                            .data(result)
                            .build()
                    );
        }
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Result.builder(HttpStatus.INTERNAL_SERVER_ERROR)
                            .message("任务创建失败")
                            .build()
                    );
        }
    }

    @SaCheckPermission("TASK_VIEW")
    @SaCheckRole(value = {"USER", "DOCTOR", "DEPT_ADMIN"}, mode = SaMode.OR)
    @CheckScope("TASK_VIEW")
    @GetMapping
    public ResponseEntity<Result> queryTask(@RequestParam("task_id") String taskId) {
        /**
         * TODO 权限检查
         * 用户只能查看自己创建的任务
         * 医生继承用户，只能查看与自己有关联的用户创建的任务
         */

        return ResponseEntity.ok(
                Result.builder(HttpStatus.OK)
                        .message(HttpStatus.OK.getReasonPhrase())
                        .data(evalService.queryEvalTaskStatus(taskId))
                        .build()
        );

    }
}
