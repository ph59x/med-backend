package xyz.ph59.med.mq.encoder;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.lang.NonNull;
import xyz.ph59.med.mq.MessageEncoder;

import java.io.UnsupportedEncodingException;

public class PlainTextEncoder implements MessageEncoder {
    @Override
    public byte[] handle(@NonNull Object object,
                         @NonNull MessageProperties messageProperties) throws UnsupportedEncodingException {
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
        messageProperties.setContentEncoding(CHARSET);
        return object.toString().getBytes(CHARSET);
    }
}
