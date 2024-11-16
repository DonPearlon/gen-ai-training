package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorDbService {

    Mono<Object> createCollection();

    Mono<String> processAndSaveText(VectorDbRequest request);

    Flux<String> processAndSaveText(List<VectorDbRequest> requests);

    Mono<List<VectorDbResponse>> search(VectorDbRequest request);
}
