package xyz.ph59.med.chatbot.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
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

    @Bean
    public ContentRetriever vectorContentRetriever(OpenAiEmbeddingModel openAiEmbeddingModel, EmbeddingStore<TextSegment> embeddingStore) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(openAiEmbeddingModel)
                .embeddingStore(embeddingStore)
                .maxResults(6)
                .build();
    }
}
