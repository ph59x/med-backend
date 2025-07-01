package xyz.ph59.med.config;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import xyz.ph59.med.service.McpService;

@Configuration
public class McpToolConfig {

    @Bean
    public ToolCallbackProvider dataAnalysisTools(McpService mcpService) {
        return MethodToolCallbackProvider.builder().toolObjects(mcpService).build();
    }
}
