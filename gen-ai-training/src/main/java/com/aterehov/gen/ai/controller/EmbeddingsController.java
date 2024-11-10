package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.service.EmbeddingsService;
import com.azure.ai.openai.models.Embeddings;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/embeddings")
@RequiredArgsConstructor
public class EmbeddingsController {

    private final EmbeddingsService embeddingsService;

    @PostMapping
    public Mono<Embeddings> getEmbeddings(@RequestBody final VectorDbRequest request) {
        return embeddingsService.retrieveEmbeddings(request.text());
    }
}
