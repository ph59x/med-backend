package xyz.ph59.med.mq;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.lang.NonNull;

import java.io.UnsupportedEncodingException;

public interface MessageEncoder extends MessageHandler {
    default byte[] handle(@NonNull Object object,
                          @NonNull MessageProperties messageProperties) throws UnsupportedEncodingException {
        throw new UnsupportedOperationException(
                "未注册针对\"" + object.getClass().getCanonicalName() + "\"的处理类");
    }
}
