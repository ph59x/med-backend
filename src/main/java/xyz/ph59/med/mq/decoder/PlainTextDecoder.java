package xyz.ph59.med.mq.decoder;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.converter.MessageConversionException;
import xyz.ph59.med.mq.MessageDecoder;

import java.io.UnsupportedEncodingException;

public class PlainTextDecoder implements MessageDecoder {
    @Override
    public Object handle(Message message) {
        try {
            return new String(message.getBody(), CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException("Failed to convert Message content", e);
        }
    }
}
