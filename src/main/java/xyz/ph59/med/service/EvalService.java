package xyz.ph59.med.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.ph59.med.config.RabbitConfig;
import xyz.ph59.med.entity.EvalResult;
import xyz.ph59.med.entity.EvalTaskInfo;
import xyz.ph59.med.mapper.EvalTaskMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvalService {
    private final RabbitTemplate rabbitTemplate;
    private final InfluxService influxService;
    private final EvalTaskMapper evalTaskMapper;

    // TODO 故障处理
    public EvalResult createTask(int uid, ZonedDateTime start, ZonedDateTime end) {
        //查询数据
        List<Short[]> messageData = influxService.queryForEval(uid, start, end);

        //构建消息
        UUID messageId = UUID.randomUUID();
        Message message = MessageBuilder.withBody(JSON.toJSONBytes(messageData))
                .setCorrelationId(String.valueOf(messageId))
                .setContentEncoding("UTF-8")
                .setTimestamp(Date.from(Instant.now()))
                .setReplyTo("amq.rabbitmq.reply-to")
                .build();

        EvalTaskInfo taskInfo = new EvalTaskInfo();
        taskInfo.setTargetId(Long.valueOf(uid));
        taskInfo.setTaskId(messageId.toString());
        taskInfo.setTargetTimeStart(start.toLocalDateTime());
        taskInfo.setTargetTimeEnd(end.toLocalDateTime());
        // TODO 处理调用者与目标用户不同的情况
        taskInfo.setCallerId(Long.valueOf(uid));
        taskInfo.setCreateTime(LocalDateTime.now());

        evalTaskMapper.insertTask(taskInfo);

        JSONObject json = (JSONObject) rabbitTemplate.convertSendAndReceive(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                message
        );
        EvalResult evalResult = json.to(EvalResult.class);

        if (evalResult.isSuccess()) {
            taskInfo.setStatus("SUCCESS");
            taskInfo.setResult(evalResult.getType());
        }
        else {
            taskInfo.setStatus("FAIL");
        }
        taskInfo.setEvalCostTime(evalResult.getTimeCost());
        taskInfo.setEndTime(LocalDateTime.now());

        evalTaskMapper.updateTaskResult(taskInfo);

        return evalResult;
    }
}
