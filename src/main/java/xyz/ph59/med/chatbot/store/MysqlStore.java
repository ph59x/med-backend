package xyz.ph59.med.chatbot.store;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import xyz.ph59.med.chatbot.mapper.ChatMemoryMapper;

import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MysqlStore implements ChatMemoryStore {
    private final ChatMemoryMapper mapper;

    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String content = mapper.queryContent((String) memoryId);

        if (content == null) {
            return new LinkedList<>();
        }
        return ChatMessageDeserializer.messagesFromJson(content);
    }

    @Override
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        if (mapper.chatExist((String) memoryId)) {
            mapper.updateChat(
                    (String) memoryId,
                    LocalDateTime.now(),
                    ChatMessageSerializer.messagesToJson(messages)
            );
        }
        else {
            mapper.createChat(
                    0,// TODO 获取并写入用户id
                    (String) memoryId,
                    LocalDateTime.now(),
                    ChatMessageSerializer.messagesToJson(messages)
            );
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        mapper.removeContent((String) memoryId);
    }
}
