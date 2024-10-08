package com.aterehov.gen.ai.controller;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    /*@PostMapping("/chat")
    public Mono<String> getResponse(@RequestBody final Mono<ChatBotRequest> chatBotRequest) {
        return chatBotService.getResponse(chatBotRequest);
    }*/

    @PostMapping("/chat")
    public String getResponse(@RequestBody final ChatBotRequest chatBotRequest) {
        return chatBotService.getResponse(chatBotRequest);
    }
}
