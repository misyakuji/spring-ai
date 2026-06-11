package com.misyakuji.ai.controller;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/mcp")
@ConditionalOnProperty(name = "spring.ai.mcp.client.enabled", havingValue = "true")
@RequiredArgsConstructor
public class McpController {

    private final ToolCallbackProvider toolCallbackProvider;

    @GetMapping("/servers")
    public Map<String, Object> listMcpServers() {
        var tools = toolCallbackProvider.getToolCallbacks();
        Map<String, Object> result = new HashMap<>();
        result.put("status", "connected");
        result.put("toolCount", tools.length);
        result.put("tools", java.util.Arrays.stream(tools)
                .map(t -> Map.of("name", t.getToolDefinition().name(), "description", t.getToolDefinition().description()))
                .toList());
        return result;
    }
}
