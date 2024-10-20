package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatBotService {

    Flux<String> getResponse(Mono<ChatBotRequest> chatBotRequest);

    Mono<String> getResponseKernelFunctionJson(Mono<ChatBotRequest> chatBotRequest);

    Mono<ChatBotResponse> getResponseKernelFunction(Mono<ChatBotRequest> chatBotRequest);

    Mono<String> getConversationSummary();
}
