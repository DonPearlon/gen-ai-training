package com.aterehov.gen.ai.service.impl;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.service.ChatBotService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SemanticKernelChatBotService implements ChatBotService {

    private final Kernel kernel;

    private final ChatCompletionService chatCompletionService;

    private InvocationContext invocationContext;

    private ChatHistory chatHistory;

    @PostConstruct
    public void init() {
        invocationContext = new InvocationContext.Builder()
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
        chatHistory = new ChatHistory();
    }

    @Override
    public Mono<String> getResponse(Mono<ChatBotRequest> chatBotRequest) {

        return chatBotRequest
                .flatMap(request -> generateResponseFromAI(request.input()))
                .flatMap(contentList -> {
                    if (contentList.isEmpty()) {
                        return Mono.empty();
                    }
                    ChatMessageContent<?> firstContent = contentList.getFirst();
                    return firstContent.getContent() != null ? Mono.just(firstContent.getContent()) : Mono.empty();
                });
    }

    private Mono<List<ChatMessageContent<?>>> generateResponseFromAI(String input) {
        chatHistory.addUserMessage(input);
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
    }
}
