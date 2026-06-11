package com.misyakuji.ai.service;

import com.misyakuji.ai.dto.RagChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
@RequiredArgsConstructor
public class RagChatService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    public Flux<String> ragChat(RagChatRequest request) {
        FilterExpressionBuilder fb = new FilterExpressionBuilder();
        var filter = fb.eq("knowledgeBaseId", request.getKnowledgeBaseId()).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(request.getQuestion())
                .topK(request.getTopK() != null ? request.getTopK() : 5)
                .similarityThreshold(0.7)
                .filterExpression(filter)
                .build();

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequest)
                .build();

        String sessionId = request.getSessionId() != null ? request.getSessionId() : request.getKnowledgeBaseId();

        return chatClient.prompt()
                .user(request.getQuestion())
                .advisors(a -> a.advisors(qaAdvisor).param(CONVERSATION_ID, sessionId))
                .stream()
                .content();
    }
}
