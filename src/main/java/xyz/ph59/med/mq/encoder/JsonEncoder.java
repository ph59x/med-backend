package xyz.ph59.med.mq.encoder;

import com.alibaba.fastjson2.JSON;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.lang.NonNull;
import xyz.ph59.med.mq.MessageEncoder;

import java.io.UnsupportedEncodingException;

public class JsonEncoder implements MessageEncoder {
    @Override
    public byte[] handle(@NonNull Object object,
                         @NonNull MessageProperties messageProperties) throws UnsupportedEncodingException {
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        return JSON.toJSONString(object).getBytes(CHARSET);
    }
}
