package com.flywhl.saa.knowledgeqa.rag;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;

import com.flywhl.saa.knowledgeqa.model.entity.KbChunk;
import com.flywhl.saa.knowledgeqa.model.vo.CitationVO;
import com.flywhl.saa.knowledgeqa.repository.KbChunkRepository;

/**
 * CitationPostProcessor 单元测试：metadata → CitationVO 映射与 snippet 截断。
 *
 * @author flywhl
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("引用溯源后处理")
class CitationPostProcessorTest {

    @Mock
    private KbChunkRepository chunkRepository;

    private CitationPostProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CitationPostProcessor(chunkRepository);
    }

    @Test
    @DisplayName("metadata 完整时映射为 CitationVO")
    void toCitationsMapsMetadata() {
        Document doc = Document.builder()
                .text("住宿费上限 600 元/晚")
                .metadata(Map.of(
                        "documentId", 1L,
                        "chunkId", 42L,
                        "title", "差旅制度"))
                .score(0.88)
                .build();

        List<CitationVO> citations = processor.toCitations(List.of(doc));

        assertThat(citations).hasSize(1);
        CitationVO citation = citations.getFirst();
        assertThat(citation.documentId()).isEqualTo(1L);
        assertThat(citation.chunkId()).isEqualTo(42L);
        assertThat(citation.documentTitle()).isEqualTo("差旅制度");
        assertThat(citation.snippet()).contains("600");
        assertThat(citation.score()).isEqualTo(0.88);
    }

    @Test
    @DisplayName("chunkId 缺失时回查 kb_chunk")
    void resolvesChunkIdFromRepository() {
        KbChunk chunk = new KbChunk();
        chunk.setId(99L);
        when(chunkRepository.findByMilvusPk("milvus-1")).thenReturn(Optional.of(chunk));

        Document doc = Document.builder()
                .id("milvus-1")
                .text("片段文本")
                .metadata(Map.of("documentId", 2L, "title", "手册"))
                .score(0.75)
                .build();

        CitationVO citation = processor.toCitations(List.of(doc)).getFirst();

        assertThat(citation.chunkId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("超长 snippet 截断至 240 字")
    void truncatesLongSnippet() {
        String longText = "x".repeat(300);
        Document doc = new Document(longText, Map.of("documentId", 1L));

        CitationVO citation = processor.toCitations(List.of(doc)).getFirst();

        assertThat(citation.snippet()).hasSize(241);
        assertThat(citation.snippet()).endsWith("…");
    }

    @Test
    @DisplayName("空文档列表返回空引用")
    void emptyDocumentsReturnsEmpty() {
        assertThat(processor.toCitations(List.of())).isEmpty();
        assertThat(processor.toCitations(null)).isEmpty();
    }
}
