package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import com.aterehov.gen.ai.service.EmbeddingsService;
import com.aterehov.gen.ai.service.VectorDbService;
import com.azure.ai.openai.models.Embeddings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/embeddings")
@RequiredArgsConstructor
public class EmbeddingsController {

    private final EmbeddingsService embeddingsService;

    private final VectorDbService vectorDbService;

    @PostMapping
    public Mono<Embeddings> getEmbeddings(@RequestBody final VectorDbRequest request) {
        return embeddingsService.retrieveEmbeddings(request.text());
    }

    @PostMapping("/create-collection")
    public Mono<String> createCollection() {
        return vectorDbService.createCollection();
    }

    @PostMapping("/save-text")
    public Mono<String> saveText(@RequestBody final VectorDbRequest request) {
        return vectorDbService.processAndSaveText(request);
    }

    @PostMapping("/search")
    public Mono<List<VectorDbResponse>> search(@RequestBody final VectorDbRequest request) {
        return vectorDbService.search(request);
    }

}
