package com.misyakuji.ai.service;

import com.misyakuji.ai.entity.KnowledgeBase;
import com.misyakuji.ai.entity.KnowledgeDocument;
import com.misyakuji.ai.repository.KnowledgeBaseRepository;
import com.misyakuji.ai.repository.KnowledgeDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class KnowledgeImportService {

    private final VectorStore vectorStore;
    private final KnowledgeBaseRepository kbRepo;
    private final KnowledgeDocumentRepository docRepo;

    public KnowledgeDocument importDocument(MultipartFile file, String knowledgeBaseId) {
        KnowledgeBase kb = kbRepo.findById(knowledgeBaseId)
                .orElseThrow(() -> new RuntimeException("知识库不存在: " + knowledgeBaseId));

        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setId(UUID.randomUUID().toString());
        doc.setKnowledgeBaseId(knowledgeBaseId);
        doc.setFileName(file.getOriginalFilename());
        doc.setFileType(getFileType(file.getOriginalFilename()));
        doc.setFileSize(file.getSize());
        doc.setStatus("PROCESSING");
        docRepo.save(doc);

        try {
            List<Document> rawDocs = new TextReader(file.getResource()).get();

            TokenTextSplitter splitter = TokenTextSplitter.builder()
                    .withChunkSize(512)
                    .withMinChunkSizeChars(100)
                    .withMinChunkLengthToEmbed(5)
                    .withMaxNumChunks(1000)
                    .withKeepSeparator(true)
                    .build();
            List<Document> chunks = splitter.apply(rawDocs);

            chunks.forEach(chunk -> {
                chunk.getMetadata().put("knowledgeBaseId", knowledgeBaseId);
                chunk.getMetadata().put("fileName", file.getOriginalFilename());
                chunk.getMetadata().put("documentId", doc.getId());
            });

            vectorStore.add(chunks);

            doc.setChunkCount(chunks.size());
            doc.setStatus("DONE");
            docRepo.save(doc);

            log.info("Imported document '{}' into knowledge base '{}', {} chunks",
                    file.getOriginalFilename(), knowledgeBaseId, chunks.size());
        } catch (Exception e) {
            doc.setStatus("FAILED");
            doc.setErrorMsg(e.getMessage());
            docRepo.save(doc);
            log.error("Failed to import document '{}'", file.getOriginalFilename(), e);
        }

        return doc;
    }

    private String getFileType(String filename) {
        if (filename == null) return "unknown";
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase() : "unknown";
    }
}
