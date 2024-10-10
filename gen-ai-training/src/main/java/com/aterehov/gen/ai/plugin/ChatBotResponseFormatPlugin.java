package com.aterehov.gen.ai.plugin;

import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import com.microsoft.semantickernel.text.TextChunker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class ChatBotResponseFormatPlugin {

    private static final int MAX_TOKENS = 1024;

    private static final double TEMPERATURE = 0.7;

    private static final double TOP_P = 0.5;

    public static final String JSON_FORMATTING = """
            {{$INPUT}}. Response should be in the following format (JSON), no other symbols allowed:\
             {
                "response": {Generated Response}
            }
            """.stripIndent();

    private final KernelFunction<String> jsonFormatFunction;

    private final KernelFunction<ChatBotResponse> responseObjectFunction;

    public ChatBotResponseFormatPlugin() {
        var settings = PromptExecutionSettings.builder()
                .withMaxTokens(MAX_TOKENS)
                .withTemperature(TEMPERATURE)
                .withTopP(TOP_P)
                .build();

        this.jsonFormatFunction = KernelFunction
                .<String>createFromPrompt(JSON_FORMATTING)
                .withDefaultExecutionSettings(settings)
                .withName("jsonFormat")
                .withDescription(
                        "Return response in JSON format")
                .build();

        this.responseObjectFunction = KernelFunction
                .<ChatBotResponse>createFromPrompt("{{$INPUT}}")
                .withDefaultExecutionSettings(settings)
                .withName("responseObject")
                .withDescription(
                        "Return response as an object")
                .build();
    }

    @DefineKernelFunction(description = "Any input given, return response in JSON format.",
            name = "jsonFormat", returnType = "java.lang.String")
    public Mono<String> jsonFormat(
            @KernelFunctionParameter(description = "Any input.", name = "input") String input,
            Kernel kernel) {
        return processAsyncJson(this.jsonFormatFunction, input, kernel);
    }

    @DefineKernelFunction(description = "Return result in form of chat bot response object",
            name = "responseObject", returnType = "com.aterehov.gen.ai.dto.ChatBotResponse")
    public Mono<ChatBotResponse> responseObject(
            @KernelFunctionParameter(description = "Any input.", name = "input") String input,
            Kernel kernel) {
        return processAsyncResponseObject(this.responseObjectFunction, input, kernel);
    }

    private static Mono<String> processAsyncJson(KernelFunction<String> func, String input,
                                                 Kernel kernel) {
        List<String> lines = TextChunker.splitPlainTextLines(input, MAX_TOKENS);
        List<String> paragraphs = TextChunker.splitPlainTextParagraphs(lines, MAX_TOKENS);

        return Flux.fromIterable(paragraphs)
                .concatMap(paragraph -> func.invokeAsync(kernel)
                        .withArguments(
                                new KernelFunctionArguments.Builder()
                                        .withInput(paragraph)
                                        .build())
                        .withResultType(
                                ContextVariableTypes.getGlobalVariableTypeForClass(String.class)))
                .reduce("", (acc, next) ->
                        acc + "\n" + next.getResult()
                );
    }

    private static Mono<ChatBotResponse> processAsyncResponseObject(KernelFunction<ChatBotResponse> func, String input,
                                             Kernel kernel) {
        List<String> lines = TextChunker.splitPlainTextLines(input, MAX_TOKENS);
        List<String> paragraphs = TextChunker.splitPlainTextParagraphs(lines, MAX_TOKENS);

        return Flux.fromIterable(paragraphs)
                .concatMap(paragraph -> func.invokeAsync(kernel)
                        .withArguments(
                                new KernelFunctionArguments.Builder()
                                        .withInput(paragraph)
                                        .build())
                        .withResultType(
                                ContextVariableTypes.getGlobalVariableTypeForClass(String.class)))
                .reduce("", (acc, next) ->
                        acc + "\n" + next.getResult()
                ).map(ChatBotResponse::new);
    }
}
