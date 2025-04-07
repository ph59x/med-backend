package xyz.ph59.med.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping
    public ResponseEntity<Result> createEvalTask(@RequestParam("start") String startTimeStr,
                                                 @RequestParam("end") String endTimeStr,
                                                 @RequestParam("uid") Integer uid) {

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
            uid = (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }
        // TODO 指定UID与请求发起者不同时的权限检查

        UUID taskId = evalService.createTask(uid, start, end);

        // TODO 任务创建故障处理

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.builder(HttpStatus.CREATED)
                        .message("Created evaluation task.")
                        .data(taskId)
                        .build()
                );
    }
}
