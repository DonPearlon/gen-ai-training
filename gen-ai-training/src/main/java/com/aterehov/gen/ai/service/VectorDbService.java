package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import reactor.core.publisher.Mono;

import java.util.List;

public interface VectorDbService {

    Mono<String> createCollection();

    Mono<String> processAndSaveText(VectorDbRequest request);

    Mono<List<VectorDbResponse>> search(VectorDbRequest request);
}
