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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SemanticKernelChatBotService implements ChatBotService {

    public static final String FORMATTING = """
            . Response should be in the following format (JSON), no other symbols allowed:\
             {
                "response": {Generated Response}
            }
            """;


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
                .map(ChatMessageContent::getContent)
                .onErrorMap(this::handleException);
    }

    private Mono<List<ChatMessageContent<?>>> generateResponseFromAI(String input) {
        chatHistory.addUserMessage(input + FORMATTING);
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
    }

    private ResponseStatusException handleException(Throwable exception) {
        if (exception instanceof WebClientResponseException webClientEx) {
            var status = webClientEx.getStatusCode();
            if (status.is4xxClientError()) {
                return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request to downstream service", exception);
            } else if (status.is5xxServerError()) {
                return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error in downstream service", exception);
            }
        }
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", exception);
    }
}
