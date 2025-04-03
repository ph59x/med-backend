package xyz.ph59.med.controller;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import xyz.ph59.med.config.RabbitConfig;
import xyz.ph59.med.entity.Result;
import xyz.ph59.med.service.InfluxService;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/eval")
@RequiredArgsConstructor
public class EvalController {
    private final RabbitTemplate rabbitTemplate;
    private final InfluxService influxService;

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

        // TODO 指定UID与请求发起者不同时的权限检查
        if (uid == null) {
            uid = (int) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        List<Short[]> messageData = influxService.queryForEval(uid, start, end);
        UUID messageId = UUID.randomUUID();
        Message message = MessageBuilder.withBody(JSON.toJSONBytes(messageData))
                .setMessageId(String.valueOf(messageId))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setTimestamp(Date.from(Instant.now()))
                .build();

        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Result.builder(HttpStatus.CREATED)
                        .message("Created evaluation task.")
                        .data(messageId)
                        .build()
                );
    }
}
