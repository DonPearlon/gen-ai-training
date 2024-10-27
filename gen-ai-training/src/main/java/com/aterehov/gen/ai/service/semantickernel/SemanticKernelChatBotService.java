package com.aterehov.gen.ai.service.semantickernel;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.aterehov.gen.ai.dto.SystemMessageRequest;
import com.aterehov.gen.ai.exception.NotFoundException;
import com.aterehov.gen.ai.service.ChatBotService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionFromPrompt;
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
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SemanticKernelChatBotService implements ChatBotService {

    public static final String DEFAULT_SYSTEM_MESSAGE = "You are a helpful assistant.";

    private final Map<String, ChatCompletionService> modelToChatCompletionService;

    private final Map<String, Kernel> modelToKernel;

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
    public Flux<ChatBotResponse> getResponse(Mono<ChatBotRequest> chatBotRequest, String model) {
        return chatBotRequest
                .flatMapMany(request -> generateResponseFromAI(request.input(), model))
                .flatMapIterable(Function.identity())
                .map(ChatMessageContent::getContent)
                .map(ChatBotResponse::new)
                .onErrorMap(this::handleException);
    }

    private Mono<List<ChatMessageContent<?>>> generateResponseFromAI(String input, String modelName) {
        ChatCompletionService chatCompletionService = modelToChatCompletionService.get(modelName);
        Kernel kernel = modelToKernel.get(modelName);
        if (chatCompletionService == null || kernel == null) {
            return Mono.error(new NotFoundException("%s model was not found".formatted(modelName)));
        }
        if (modelName.contains("gpt")) {
            this.chatHistory.addUserMessage(input);
            return chatCompletionService
                    .getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
        }
        return chatCompletionService
                .getChatMessageContentsAsync(input, kernel, invocationContext);
    }

    @Override
    public Mono<ChatBotResponse> getConversationSummary() {
        Kernel kernel = modelToKernel.get("gpt-4-turbo");
        if (kernel == null) {
            return Mono.error(new NotFoundException("%s model was not found".formatted("gpt-4-turbo")));
        }
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
