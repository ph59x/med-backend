package xyz.ph59.med.chatbot.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeEmbeddingStore;
import dev.langchain4j.store.embedding.pinecone.PineconeServerlessIndexConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EmbeddingConfig {

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
        return PineconeEmbeddingStore.builder()
                .apiKey("pcsk_3PBiR9_LQyYvmeHvkwMC6dBUCdJNmWVt39qKT5LHgVv1kZcmXGbNM8LxDgYgecn4SF6vWd")
                .index("dev")
                /*
                坑点：api的 default namespace 字段值与平台的不一样
                "default" <-> "__default__"
                导致无法进行向量匹配
                 */
                .nameSpace("litetest")
                .createIndex(PineconeServerlessIndexConfig.builder()
                        .cloud("AWS")
                        .region("us-east-1")
                        .dimension(1024)
                        .build()
                )
                .build();
    }
}
