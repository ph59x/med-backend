package xyz.ph59.med.service;

import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import xyz.ph59.med.config.RabbitConfig;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EvalService {
    private final RabbitTemplate rabbitTemplate;
    private final InfluxService influxService;

    // TODO 故障处理
    public Object createTask(int uid, ZonedDateTime start, ZonedDateTime end) {
        //查询数据
        List<Short[]> messageData = influxService.queryForEval(uid, start, end);

        //构建并发送消息
        UUID messageId = UUID.randomUUID();
        Message message = MessageBuilder.withBody(JSON.toJSONBytes(messageData))
                .setCorrelationId(String.valueOf(messageId))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding("UTF-8")
                .setTimestamp(Date.from(Instant.now()))
                .setReplyTo("amq.rabbitmq.reply-to")
                .build();

        return rabbitTemplate.convertSendAndReceive(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY, message);
    }
}
