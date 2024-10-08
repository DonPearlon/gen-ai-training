package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import reactor.core.publisher.Mono;

public interface ChatBotService {
    Mono<ChatBotResponse> getResponse(Mono<ChatBotRequest> chatBotRequest);
}
