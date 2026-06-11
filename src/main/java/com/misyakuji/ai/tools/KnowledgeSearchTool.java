package com.misyakuji.ai.tools;

import com.misyakuji.ai.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class KnowledgeSearchTool {

    private final KnowledgeSearchService searchService;

    @Tool(description = "在知识库中搜索相关文档片段，用于回答用户关于特定知识库内容的问题")
    public String searchKnowledge(
            @ToolParam(description = "搜索关键词或问题") String query,
            @ToolParam(description = "知识库ID") String knowledgeBaseId) {
        List<Map<String, Object>> results = searchService.search(query, knowledgeBaseId, 5, 0.7);
        if (results.isEmpty()) {
            return "未找到相关文档片段";
        }
        return results.stream()
                .map(r -> (String) r.get("content"))
                .collect(Collectors.joining("\n---\n"));
    }
}
