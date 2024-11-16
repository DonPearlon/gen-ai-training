package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import com.aterehov.gen.ai.service.JsonDataService;
import com.aterehov.gen.ai.service.VectorDbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/vector-db")
@RequiredArgsConstructor
public class VectorDbController {

    private final VectorDbService vectorDbService;

    private final JsonDataService jsonDataService;

    @PostMapping("/create-collection")
    public Mono<Object> createCollection() {
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

    @PostMapping("/save-review-data")
    public Flux<String> saveReviewData() {
       List<VectorDbRequest> vectorDbRequests = jsonDataService.readVectorDbRequestFromFile("reviews.json");
       return vectorDbService.processAndSaveText(vectorDbRequests);
    }
}
