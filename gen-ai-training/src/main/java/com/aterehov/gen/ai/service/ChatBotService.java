package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.aterehov.gen.ai.dto.SystemMessageRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatBotService {

    Flux<ChatBotResponse> getResponse(Mono<ChatBotRequest> chatBotRequest, String model, String context);

    Mono<ChatBotResponse> getConversationSummary();

    void initNewChatHistory();

    void addSystemMessage(SystemMessageRequest systemMessageRequest);
}
