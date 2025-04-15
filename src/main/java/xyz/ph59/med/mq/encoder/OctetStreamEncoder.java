package xyz.ph59.med.mq.encoder;

import org.springframework.amqp.core.MessageProperties;
import org.springframework.lang.NonNull;
import xyz.ph59.med.mq.MessageEncoder;

public class OctetStreamEncoder implements MessageEncoder {
    @Override
    public byte[] handle(@NonNull Object object,
                         @NonNull MessageProperties messageProperties) {
        messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
        return (byte[]) object;
    }
}
