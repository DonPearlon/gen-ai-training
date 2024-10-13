package com.aterehov.gen.ai.service.semantickernel;

import com.aterehov.gen.ai.dto.ChatBotRequest;
import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.aterehov.gen.ai.service.ChatBotService;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.*;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
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
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class SemanticKernelChatBotService implements ChatBotService {

    public static final String JSON_FORMAT_PROMPT = """
            %s. Response should be in the following format (JSON), no other symbols allowed:\
             {
                "response": {Generated Response}
            }
            """;

    public static final String KERNEL_FUNCTION_PROMPT =
            "Just return not formatted output (no modifications) of the following function!" +
                    " {{%s input=\"%s\"}}";

    private final Kernel kernel;

    private final ChatCompletionService chatCompletionService;

    private final InvocationContext invocationContext;

    private ChatHistory chatHistory;

    @PostConstruct
    public void init() {
        this.chatHistory = new ChatHistory();
        this.chatHistory.addSystemMessage("You are a helpful assistant.");
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
        this.chatHistory.addUserMessage(JSON_FORMAT_PROMPT.formatted(input));
        return chatCompletionService
                .getChatMessageContentsAsync(chatHistory, kernel, invocationContext);
    }

    @Override
    public Mono<String> getResponseKernelFunctionJson(Mono<ChatBotRequest> chatBotRequest) {
        return chatBotRequest
                .flatMap(this::generateResponseFromAIKernelFunctionJson)
                .onErrorMap(this::handleException);
    }

    @Override
    public Mono<ChatBotResponse> getResponseKernelFunction(Mono<ChatBotRequest> chatBotRequest) {
        return chatBotRequest
                .flatMap(this::generateResponseFromAIKernelFunction)
                .onErrorMap(this::handleException);
    }

    private Mono<String> generateResponseFromAIKernelFunctionJson(ChatBotRequest chatBotRequest) {

        KernelFunction<String> prompt = KernelFunctionFromPrompt
                .<String>createFromPrompt(KERNEL_FUNCTION_PROMPT
                        .formatted("ChatBotResponseFormatPlugin.jsonFormat", chatBotRequest.input()))
                .build();
        FunctionInvocation<String> functionInvocation = prompt.invokeAsync(kernel);
        return functionInvocation.map(FunctionResult::getResult);
    }

    private Mono<ChatBotResponse> generateResponseFromAIKernelFunction(ChatBotRequest chatBotRequest) {
        KernelFunction<ChatBotResponse> prompt = KernelFunctionFromPrompt
                .<ChatBotResponse>createFromPrompt(KERNEL_FUNCTION_PROMPT
                        .formatted("ChatBotResponseFormatPlugin.responseObject", chatBotRequest.input()))
                .build();
        FunctionInvocation<ChatBotResponse> functionInvocation = prompt.invokeAsync(kernel);
        return functionInvocation.map(FunctionResult::getResult);
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
