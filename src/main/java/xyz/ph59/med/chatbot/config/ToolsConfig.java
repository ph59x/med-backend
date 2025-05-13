package xyz.ph59.med.chatbot.config;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.agent.tool.ToolMemoryId;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ToolsConfig {

    @Tool(
            name = "胎心数据异常检测",
            value = "将时间段`当前时间 - 偏移量`至`当前时间`内的数据交由专用模型进行异常检测。\n" +
                    "返回`true`表示异常\n" +
                    "返回`false`表示正常"
    )
    boolean eval(
            @ToolMemoryId String memoryId,
            @P("当前时间，需符合RFC 3339格式") String currentTime,
            @P("时间偏移量") int offset,
            @P("时间偏移单位，最高精确到秒") TimeUnit timeUnit
    ) {
        return true;
    }
}
