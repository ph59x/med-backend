package xyz.ph59.med.mq;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.lang.NonNull;
import xyz.ph59.med.mq.decoder.*;
import xyz.ph59.med.mq.encoder.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

// 基于策略模式的MessageConverter
public class MessageConverter extends AbstractMessageConverter {
    private static final String CHARSET = "UTF-8";
    private static final Map<String, MessageDecoder> DECODERS = new HashMap<>(3);
    private static final Map<Class<?>, MessageEncoder> ENCODERS = new HashMap<>(2);
    private static final MessageEncoder DEFAULT_ENCODER = new JsonEncoder();

    // 注册策略
    static {
        DECODERS.put(MessageProperties.CONTENT_TYPE_BYTES, new OctetStreamDecoder());
        DECODERS.put(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, new PlainTextDecoder());
        DECODERS.put(MessageProperties.CONTENT_TYPE_JSON, new JsonDecoder());

        ENCODERS.put(byte[].class, new OctetStreamEncoder());
        ENCODERS.put(CharSequence.class, new PlainTextEncoder());
    }

    @NonNull
    @Override
    public Object fromMessage(Message message)
            throws MessageConversionException {
        return DECODERS.getOrDefault(
                        message.getMessageProperties().getContentType(),
                        new MessageDecoder() {
                        })
                .handle(message);
    }

    @NonNull
    @Override
    protected Message createMessage(@NonNull Object objectToConvert,
                                    @NonNull MessageProperties messageProperties)
            throws MessageConversionException {
        byte[] bytes;
        try {
            bytes = ENCODERS.getOrDefault(objectToConvert.getClass(), DEFAULT_ENCODER)
                    .handle(objectToConvert, messageProperties);
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException(
                    "Failed to convert Message content", e);
        }
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }

}