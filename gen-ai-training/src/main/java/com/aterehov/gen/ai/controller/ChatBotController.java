package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.aterehov.gen.ai.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PostMapping(value = "/v1/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<String> getResponseV1(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        return chatBotService.getResponse(chatBotRequest);
    }

    @PostMapping(value = "/v2/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<String> getResponseV2(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        return chatBotService.getResponseKernelFunctionJson(chatBotRequest);
    }

    @PostMapping(value = "/v3/chat", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ChatBotResponse> getResponseV3(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        return chatBotService.getResponseKernelFunction(chatBotRequest);
    }
}
