package xyz.ph59.med.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ph59.med.annotation.CheckScope;
import xyz.ph59.med.entity.DataPoint;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.tsdb.TsdbConnector;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/data")
@RequiredArgsConstructor
public class DataController {
    private final TsdbConnector tsdbConnector;

    @SaCheckRole("USER")
    @SaCheckPermission("DATA_ACCESS")
    @PostMapping
    public ResponseEntity<?> writeData(@RequestBody String request) {
        try {
            List<DataPoint> points = JSON.parseArray(request).toJavaList(DataPoint.class);

            if (points == null || points.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        Result.builder(HttpStatus.BAD_REQUEST)
                                .message("Invalid input format.")
                                .build()
                );
            }

            int uid = StpUtil.getLoginIdAsInt();

            tsdbConnector.write(uid, points);

            return ResponseEntity.accepted().build();
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(
                    Result.builder(HttpStatus.BAD_REQUEST)
                            .message("Invalid JSON Array format.")
                            .build()
            );
        }
    }

    // 难点 时区处理
    @SaCheckPermission("DATA_ACCESS")
    @SaCheckRole(value = {"USER", "DOCTOR", "DEPT_ADMIN"}, mode = SaMode.OR)
    @CheckScope("DATA_ACCESS")
    @GetMapping
    public ResponseEntity<?> queryData(
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

        List<DataPoint> query = tsdbConnector.query(uid, start, end);

        return ResponseEntity.ok(
                Result.builder(HttpStatus.OK)
                        .message("OK")
                        .data(query)
                        .build()
        );
    }
}
