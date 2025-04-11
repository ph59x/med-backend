package xyz.ph59.med.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaMode;
import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import xyz.ph59.med.entity.DataPoint;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.service.InfluxService;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@RestController
@RequestMapping("/data")
public class DataController {
    private final InfluxService influxService;

    public DataController(InfluxService influxService) {
        this.influxService = influxService;
    }

    @SaCheckRole("USER")
    @SaCheckPermission("DATA_ACCESS")
    @PostMapping
    public ResponseEntity<?> writeData(@RequestBody String request) {
        try {
            List<DataPoint> points = JSON.parseArray(request).toJavaList(DataPoint.class);

            if (points == null) {
                return ResponseEntity.badRequest().body(
                        Result.builder(HttpStatus.BAD_REQUEST)
                                .message("Invalid JSON Array format.")
                                .build()
                );
            }

            int uid = StpUtil.getLoginIdAsInt();

            influxService.write(points, uid);

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
    @GetMapping
    public ResponseEntity<?> queryData(@RequestParam("start") String startTimeStr,
                                       @RequestParam("end") String endTimeStr) {
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

        int uid = StpUtil.getLoginIdAsInt();

        List<DataPoint> query = influxService.query(uid, start, end);

        return ResponseEntity.ok(
                Result.builder(HttpStatus.OK)
                        .message("OK")
                        .data(query)
                        .build()
        );
    }
}
