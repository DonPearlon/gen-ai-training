package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.SystemMessageRequest;
import com.aterehov.gen.ai.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    /**
     * Chat with AI model with usage of ChatCompletionService and ChatHistory
     */
    @PostMapping(value = "/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getResponse(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        return chatBotService.getResponse(chatBotRequest);
    }

    /**
     * Summary of the conversation based on ChatHistory with usage of KernelFunction
     */
    @GetMapping(value = "/summary", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getSummary() {
        return chatBotService.getConversationSummary();
    }

    /**
     * Clear ChatHistory
     */
    @PostMapping(value = "/chat/clear-history", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getClearChatHistory(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        chatBotService.initNewChatHistory();
        return ResponseEntity.ok().build();
    }

    /**
     * Add system message to ChatHistory
     */
    @PostMapping(value = "/chat/system-message", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> setSystemMessage(@RequestBody final SystemMessageRequest systemMessageRequest) {
        chatBotService.addSystemMessage(systemMessageRequest);
        return ResponseEntity.ok().build();
    }
}
