package com.aterehov.gen.ai.service.impl;

import com.aterehov.gen.ai.service.EmbeddingsService;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingsServiceImpl implements EmbeddingsService {

    @Value("${transformer-deployment-name}")
    private String transformerDeploymentName;

    private final OpenAIAsyncClient openAIAsyncClient;

    @Override
    public Mono<Embeddings> retrieveEmbeddings(String text) {
        var qembeddingsOptions = new EmbeddingsOptions(List.of(text));
        return openAIAsyncClient.getEmbeddings(transformerDeploymentName, qembeddingsOptions);
    }
}
