package xyz.ph59.med.mq;

import org.springframework.amqp.core.Message;

public interface MessageDecoder extends MessageHandler{

    default Object handle(Message message) {
        throw new UnsupportedOperationException(
                "未注册针对\"" + message.getMessageProperties().getContentType() + "\"的处理类");
    }
}
