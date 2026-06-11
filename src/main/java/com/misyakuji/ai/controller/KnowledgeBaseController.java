package com.misyakuji.ai.controller;

import com.misyakuji.ai.dto.*;
import com.misyakuji.ai.entity.KnowledgeBase;
import com.misyakuji.ai.entity.KnowledgeDocument;
import com.misyakuji.ai.repository.KnowledgeBaseRepository;
import com.misyakuji.ai.repository.KnowledgeDocumentRepository;
import com.misyakuji.ai.service.KnowledgeImportService;
import com.misyakuji.ai.service.KnowledgeSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/knowledge")
@RequiredArgsConstructor
public class KnowledgeBaseController {

    private final KnowledgeBaseRepository kbRepo;
    private final KnowledgeDocumentRepository docRepo;
    private final KnowledgeImportService importService;
    private final KnowledgeSearchService searchService;

    @PostMapping("/bases")
    public KnowledgeBaseResponse createKnowledgeBase(@Valid @RequestBody KnowledgeBaseCreateRequest request) {
        KnowledgeBase kb = new KnowledgeBase();
        kb.setId(UUID.randomUUID().toString());
        kb.setName(request.getName());
        kb.setDescription(request.getDescription());
        if (request.getEmbedModel() != null) {
            kb.setEmbedModel(request.getEmbedModel());
        }
        kbRepo.save(kb);
        return toKbResponse(kb);
    }

    @GetMapping("/bases")
    public List<KnowledgeBaseResponse> listKnowledgeBases() {
        return kbRepo.findAll().stream().map(this::toKbResponse).toList();
    }

    @GetMapping("/bases/{id}")
    public KnowledgeBaseResponse getKnowledgeBase(@PathVariable String id) {
        KnowledgeBase kb = kbRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("知识库不存在: " + id));
        return toKbResponse(kb);
    }

    @DeleteMapping("/bases/{id}")
    public void deleteKnowledgeBase(@PathVariable String id) {
        kbRepo.deleteById(id);
    }

    @PostMapping("/bases/{id}/documents")
    public KnowledgeDocumentResponse uploadDocument(@PathVariable String id,
                                                     @RequestParam("file") MultipartFile file) {
        KnowledgeDocument doc = importService.importDocument(file, id);
        return toDocResponse(doc);
    }

    @GetMapping("/bases/{id}/documents")
    public List<KnowledgeDocumentResponse> listDocuments(@PathVariable String id) {
        return docRepo.findByKnowledgeBaseIdOrderByCreatedAtDesc(id).stream()
                .map(this::toDocResponse)
                .toList();
    }

    @DeleteMapping("/documents/{id}")
    public void deleteDocument(@PathVariable String id) {
        docRepo.deleteById(id);
    }

    @PostMapping("/search")
    public List<?> searchVectors(@Valid @RequestBody VectorSearchRequest request) {
        return searchService.search(
                request.getQuery(),
                request.getKnowledgeBaseId(),
                request.getTopK(),
                request.getSimilarityThreshold());
    }

    private KnowledgeBaseResponse toKbResponse(KnowledgeBase kb) {
        KnowledgeBaseResponse resp = new KnowledgeBaseResponse();
        resp.setId(kb.getId());
        resp.setName(kb.getName());
        resp.setDescription(kb.getDescription());
        resp.setEmbedModel(kb.getEmbedModel());
        resp.setCreatedAt(kb.getCreatedAt());
        return resp;
    }

    private KnowledgeDocumentResponse toDocResponse(KnowledgeDocument doc) {
        KnowledgeDocumentResponse resp = new KnowledgeDocumentResponse();
        resp.setId(doc.getId());
        resp.setKnowledgeBaseId(doc.getKnowledgeBaseId());
        resp.setFileName(doc.getFileName());
        resp.setFileType(doc.getFileType());
        resp.setFileSize(doc.getFileSize());
        resp.setChunkCount(doc.getChunkCount());
        resp.setStatus(doc.getStatus());
        resp.setErrorMsg(doc.getErrorMsg());
        resp.setCreatedAt(doc.getCreatedAt());
        return resp;
    }
}
