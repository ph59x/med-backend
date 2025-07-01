package xyz.ph59.med.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;
import xyz.ph59.med.entity.eval.EvalResult;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class McpService {
    private final EvalService evalService;

    @Tool(
            name = "胎心数据异常检测",
            description = "将时间段`当前时间 - 偏移量`至`当前时间`内的数据交由专用模型进行异常检测。\n" +
                    "返回`true`表示异常\n" +
                    "返回`false`表示正常"
    )
    public Boolean eval(
            @ToolParam(description = "当前时间，需符合RFC 3339格式") String currentTime,
            @ToolParam(description = "时间偏移量，非负数") int offset,
            @ToolParam(description = "时间偏移单位，最高精确到秒") ChronoUnit timeUnit
    ) {
        Integer uid = 1919;

        ZonedDateTime start, end;
        try {
            end = ZonedDateTime.parse(currentTime);
            start = end.minus(offset, timeUnit);
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return null;
        }

        String taskId = evalService.createTask(
                uid,
                uid,
                start,
                end
        );

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {

        }

        EvalResult evalResult = evalService.queryEvalTaskStatus(taskId);
        if (evalResult != null && evalResult.getStatus().equals("SUCCESS")) {
            return !evalResult.getType().equals("0");
        }

        return null;
    }
}
