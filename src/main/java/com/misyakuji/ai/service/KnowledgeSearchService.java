package com.misyakuji.ai.service;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KnowledgeSearchService {

    private final VectorStore vectorStore;

    public List<Map<String, Object>> search(String query, String knowledgeBaseId, int topK, double similarityThreshold) {
        FilterExpressionBuilder fb = new FilterExpressionBuilder();
        var filter = fb.eq("knowledgeBaseId", knowledgeBaseId).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression(filter)
                .build();

        return vectorStore.similaritySearch(searchRequest).stream()
                .map(doc -> Map.<String, Object>of(
                        "content", doc.getText(),
                        "score", doc.getMetadata().getOrDefault("score", 0.0),
                        "metadata", doc.getMetadata()))
                .toList();
    }
}
