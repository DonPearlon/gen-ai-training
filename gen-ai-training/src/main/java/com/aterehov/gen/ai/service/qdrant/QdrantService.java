package com.aterehov.gen.ai.service.qdrant;

import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import com.aterehov.gen.ai.service.EmbeddingsService;
import com.aterehov.gen.ai.service.VectorDbService;
import com.azure.ai.openai.models.Embeddings;
import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.Collections.CollectionOperationResponse;
import io.qdrant.client.grpc.Points.SearchPoints;
import io.qdrant.client.grpc.Points.PointStruct;
import io.qdrant.client.grpc.Points.ScoredPoint;
import io.qdrant.client.grpc.Points.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.ValueFactory.value;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Service
@RequiredArgsConstructor
@Slf4j
public class QdrantService implements VectorDbService {

    private static final long VECTOR_SIZE = 1536;

    private static final long SEARCH_LIMIT = 10;

    @Value("${qdrant-collection-name}")
    private String collectionName;

    private final QdrantClient qdrantClient;

    private final EmbeddingsService embeddingsService;

    @Override
    public Flux<String> processAndSaveText(List<VectorDbRequest> requests) {
        return Flux.fromIterable(requests)
                .flatMap(this::processAndSaveText)
                .onErrorResume(exception -> Flux.just("Failed save text: " + exception.getMessage()));
    }

    @Override
    public Mono<String> processAndSaveText(VectorDbRequest request) {
        return embeddingsService.retrieveEmbeddings(request.text())
                .map(embeddings -> createPointStructs(embeddings, request.text()))
                .flatMap(this::saveVector)
                .onErrorMap(exception -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to save text: ", exception));
    }

    @Override
    public Mono<List<VectorDbResponse>> search(VectorDbRequest request) {
        return embeddingsService.retrieveEmbeddings(request.text())
                .map(this::createQueryVector)
                .flatMap(this::search)
                .map(this::createResponse)
                .onErrorMap(exception -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Search failed: ", exception));
    }

    @Override
    public Mono<Object> createCollection() {
        ListenableFuture<CollectionOperationResponse> future = qdrantClient.createCollectionAsync(collectionName,
                Collections.VectorParams.newBuilder()
                        .setDistance(Collections.Distance.Cosine)
                        .setSize(VECTOR_SIZE)
                        .build());
        return toMono(future)
                .map(result -> {
                    var message = "Collection was created: [%s]".formatted(result.getResult());
                    log.info(message);
                    return message;
                });
    }


    private Mono<List<ScoredPoint>> search(List<Float> queryVector) {
        ListenableFuture<List<ScoredPoint>> future = qdrantClient
                .searchAsync(
                        SearchPoints.newBuilder()
                                .setCollectionName(collectionName)
                                .addAllVector(queryVector)
                                .setWithPayload(enable(true))
                                .setLimit(SEARCH_LIMIT)
                                .build());
        return toMono(future);
    }

    private List<Float> createQueryVector(Embeddings embeddings) {
        List<Float> queryVector = new ArrayList<>();
        embeddings.getData().forEach(embeddingItem ->
                queryVector.addAll(embeddingItem.getEmbedding())
        );
        return queryVector;
    }

    private List<VectorDbResponse> createResponse(List<ScoredPoint> scoredPoints) {
        return scoredPoints.stream()
                .map(this::createResponse)
                .toList();
    }

    private VectorDbResponse createResponse(ScoredPoint scoredPoint) {
        var text = scoredPoint.getPayloadOrDefault("text", null);
        return new VectorDbResponse(text != null ? text.getStringValue() : "none", scoredPoint.getScore());
    }


    private List<PointStruct> createPointStructs(Embeddings embeddings, String text) {
        List<List<Float>> points = new ArrayList<>();
        embeddings.getData().forEach(
                embeddingItem -> {
                    var values = new ArrayList<>(embeddingItem.getEmbedding());
                    points.add(values);
                });

        List<PointStruct> pointStructs = new ArrayList<>();
        points.forEach(point -> {
            PointStruct pointStruct = createPointStruct(point, text);
            pointStructs.add(pointStruct);
        });
        return pointStructs;
    }

    private PointStruct createPointStruct(List<Float> point, String text) {
        return PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putAllPayload(Map.of("text", value(text)))
                .build();
    }

    private Mono<String> saveVector(List<PointStruct> pointStructs) {
        ListenableFuture<UpdateResult> future = qdrantClient.upsertAsync(collectionName, pointStructs);
        return toMono(future)
                .map(result -> result.getStatus().name());
    }

    private <T> Mono<T> toMono(ListenableFuture<T> future) {
        return Mono.fromCallable(() -> {
            try {
                return future.get();
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
