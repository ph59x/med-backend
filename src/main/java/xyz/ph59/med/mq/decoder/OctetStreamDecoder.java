package xyz.ph59.med.mq.decoder;

import org.springframework.amqp.core.Message;
import xyz.ph59.med.mq.MessageDecoder;

public class OctetStreamDecoder implements MessageDecoder {
    @Override
    public Object handle(Message message) {
        return message.getBody();
    }
}
