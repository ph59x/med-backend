package xyz.ph59.med.mq.decoder;

import com.alibaba.fastjson2.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import xyz.ph59.med.mq.MessageDecoder;

import java.io.UnsupportedEncodingException;

public class JsonDecoder implements MessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(JsonDecoder.class);

    @Override
    public Object handle(Message message) {
        try {
            return JSON.parse(new String(message.getBody(), CHARSET));
        } catch (UnsupportedEncodingException e) {
            log.error("消息体编码异常", e);
        }
        return null;
    }
}
