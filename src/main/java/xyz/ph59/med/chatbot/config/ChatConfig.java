package xyz.ph59.med.chatbot.config;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.ph59.med.chatbot.store.MysqlStore;

@Configuration
public class ChatConfig {

    @Bean
    public ChatMemoryProvider sqlMemoryProvider(MysqlStore mysqlStore) {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(10)
                .chatMemoryStore(mysqlStore)
                .build();
    }
}
