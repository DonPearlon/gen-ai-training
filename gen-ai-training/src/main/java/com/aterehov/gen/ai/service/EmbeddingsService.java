package com.aterehov.gen.ai.service;

import com.azure.ai.openai.models.Embeddings;
import reactor.core.publisher.Mono;

public interface EmbeddingsService {
    Mono<Embeddings> retrieveEmbeddings(String text);
}
