package xyz.ph59.med.service;

import com.alibaba.fastjson2.JSON;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.ph59.med.config.RabbitConfig;
import xyz.ph59.med.entity.eval.EvalResult;
import xyz.ph59.med.entity.eval.EvalTaskInfo;
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
    private final DataService dataService;

    // TODO 故障处理
    public String createTask(int uid, ZonedDateTime start, ZonedDateTime end) {
        //查询数据
        List<Short[]> messageData = influxService.queryForEval(uid, start, end);

        //构建消息
        UUID messageId = UUID.randomUUID();
        Message message = MessageBuilder.withBody(JSON.toJSONBytes(messageData))
                .setMessageId(String.valueOf(messageId))
                .setContentEncoding("UTF-8")
                .setTimestamp(Date.from(Instant.now()))
                .setReplyTo(RabbitConfig.RESULT_TOPIC)
                .build();

        EvalTaskInfo taskInfo = new EvalTaskInfo();
        taskInfo.setTargetId(Long.valueOf(uid));
        taskInfo.setTaskId(messageId.toString());
        taskInfo.setTargetTimeStart(start.toLocalDateTime());
        taskInfo.setTargetTimeEnd(end.toLocalDateTime());
        // TODO 处理调用者与目标用户不同的情况
        taskInfo.setCallerId(Long.valueOf(uid));
        taskInfo.setCreateTime(LocalDateTime.now());

        dataService.writeEvalTask(taskInfo);

        rabbitTemplate.convertAndSend(
                RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY,
                message
        );

        return messageId.toString();
    }

    @RabbitListener(queues = RabbitConfig.RESULT_TOPIC)
    public void handleEvalResult(Channel channel, Message message) {
        EvalResult evalResult = JSON.parseObject(message.getBody(), EvalResult.class);

        EvalTaskInfo taskInfo = new EvalTaskInfo();
        taskInfo.setTaskId(message.getMessageProperties().getMessageId());
        taskInfo.setStatus(evalResult.getStatus());
        if (evalResult.getStatus().equals("SUCCESS")) {
            taskInfo.setResult(evalResult.getType());
            taskInfo.setEvalCostTime(evalResult.getEvalCost());
        }
        taskInfo.setEndTime(LocalDateTime.now());

        try {
            dataService.updateEvalTask(taskInfo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
            // TODO 重试策略
        }

    }

    public EvalResult queryEvalTaskStatus(String taskId) {
        EvalTaskInfo taskInfo = dataService.queryEvalTask(taskId);
        return new EvalResult(taskInfo.getStatus(), taskInfo.getEvalCostTime(), taskInfo.getResult());
    }

    public Integer queryTargetId(String taskId) throws IllegalArgumentException{
        Integer targetId = dataService.queryEvalTaskTargetId(taskId);
        if (targetId == null) {
            throw new IllegalArgumentException("任务不存在或数据库中对应的id为空");
        }
        return targetId;
    }
}
