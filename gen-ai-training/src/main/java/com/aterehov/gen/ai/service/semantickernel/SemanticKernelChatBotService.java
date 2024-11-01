package com.aterehov.gen.ai.service.semantickernel;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.aterehov.gen.ai.dto.SystemMessageRequest;
import com.aterehov.gen.ai.service.ChatBotService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
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

    public static final String DEFAULT_SYSTEM_MESSAGE = "You are a helpful assistant.";

    private final Kernel kernel;

    private final ChatCompletionService chatCompletionService;

    private final InvocationContext invocationContext;

    private ChatHistory chatHistory;

    @PostConstruct
    public void init() {
        initNewChatHistory();
    }

    @Override
    public void initNewChatHistory() {
        this.chatHistory = new ChatHistory();
        this.chatHistory.addSystemMessage(DEFAULT_SYSTEM_MESSAGE);
    }

    @Override
    public void addSystemMessage(SystemMessageRequest systemMessageRequest) {
        this.chatHistory.addSystemMessage(systemMessageRequest.message());
    }

    @Override
    public Flux<ChatBotResponse> getResponse(Mono<ChatBotRequest> chatBotRequest) {

        return chatBotRequest
                .flatMapMany(request -> generateResponseFromAI(request.input()))
                .flatMapIterable(Function.identity())
                .map(ChatMessageContent::getContent)
                .map(ChatBotResponse::new)
                .onErrorMap(this::handleException);
    }

    @Override
    public Mono<ChatBotResponse> getConversationSummary() {
        KernelFunction<String> summarizeConversation = kernel
                .getFunction("ConversationSummaryPlugin",
                        "summarizeConversation");
        var arguments = KernelFunctionArguments.builder()
                .withVariable("input", this.chatHistory)
                .build();
        return kernel.invokeAsync(summarizeConversation)
                .withArguments(arguments)
                .map(FunctionResult::getResult)
                .map(ChatBotResponse::new);
    }


    private Mono<List<ChatMessageContent<?>>> generateResponseFromAI(String input) {
        this.chatHistory.addUserMessage(input);
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
