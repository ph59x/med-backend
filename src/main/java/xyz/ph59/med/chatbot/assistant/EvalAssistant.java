package xyz.ph59.med.chatbot.assistant;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;

@AiService(
        wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "qwenChatModel",
        chatMemoryProvider = "sqlMemoryProvider",
        tools = "toolsConfig",
        contentRetriever = "vectorContentRetriever"
)
public interface EvalAssistant {

    @SystemMessage(fromResource = "chatbot/systemPrompt.txt")
    @UserMessage(fromResource = "chatbot/userPrompt.txt")
    String chat(
            @MemoryId String chatId,
            @V("message") String message
    );
}
