package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatBotService {
    Flux<String> getResponse(Mono<ChatBotRequest> chatBotRequest);
}
