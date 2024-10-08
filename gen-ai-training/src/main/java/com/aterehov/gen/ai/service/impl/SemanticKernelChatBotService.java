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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SemanticKernelChatBotService implements ChatBotService {

    public static final String FORMATTING = ". Response should be in the following format (JSON), no other symbols allowed:" +
            " {\n" +
            "    \"response\": {Generated Response}\n" +
            "}\n";


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
    public Flux<String> getResponse(Mono<ChatBotRequest> chatBotRequest) {

        return chatBotRequest
                .flatMapMany(request -> generateResponseFromAI(request.input()))
                .flatMapIterable(Function.identity())
                .map(ChatMessageContent::getContent);
    }

    private Mono<List<ChatMessageContent<?>>> generateResponseFromAI(String input) {
        chatHistory.addUserMessage(input + FORMATTING);
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
    }
}
