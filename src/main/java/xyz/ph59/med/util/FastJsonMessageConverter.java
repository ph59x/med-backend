package xyz.ph59.med.util;

import com.alibaba.fastjson2.JSON;
import org.jetbrains.annotations.NotNull;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.support.converter.AbstractMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class FastJsonMessageConverter extends AbstractMessageConverter {
    private static final String CHARSET = "UTF-8";
    private static final Map<String, Function<Message,Object>> MESSAGE_CONVERTERS = new HashMap<>();
    static {
        MESSAGE_CONVERTERS.put(MessageProperties.CONTENT_TYPE_BYTES, message -> message.getBody());
        MESSAGE_CONVERTERS.put(MessageProperties.CONTENT_TYPE_TEXT_PLAIN, message -> {
            try {
                return new String(message.getBody(), CHARSET);
            } catch (UnsupportedEncodingException e) {
                throw new MessageConversionException(
                        "Failed to convert Message content", e);
            }
        });
        MESSAGE_CONVERTERS.put(MessageProperties.CONTENT_TYPE_JSON, message -> {
            try {
                return JSON.parse(new String(message.getBody(), CHARSET));
            } catch (UnsupportedEncodingException e) {
                throw new MessageConversionException(
                        "Failed to convert Message content", e);
            }
        });
    }

    @NotNull
    @Override
    public Object fromMessage(Message message)
            throws MessageConversionException {
        MessageProperties messageProperties = message.getMessageProperties();
        return MESSAGE_CONVERTERS.get(messageProperties.getContentType()).apply(message);
    }

    @NotNull
    @Override
    protected Message createMessage(@NotNull Object objectToConvert,
                                    @NotNull MessageProperties messageProperties)
            throws MessageConversionException {
        byte[] bytes;
        try {
            if (objectToConvert instanceof byte[]){
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_BYTES);
                bytes = (byte[]) objectToConvert;
            }else if(objectToConvert instanceof CharSequence){
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_TEXT_PLAIN);
                messageProperties.setContentEncoding(CHARSET);
                bytes = objectToConvert.toString().getBytes(CHARSET);
            }else {
                messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
                String jsonString = JSON.toJSONString(objectToConvert);
                bytes = jsonString.getBytes(CHARSET);
            }
        } catch (UnsupportedEncodingException e) {
            throw new MessageConversionException(
                    "Failed to convert Message content", e);
        }
        messageProperties.setContentLength(bytes.length);
        return new Message(bytes, messageProperties);
    }

}