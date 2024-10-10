package com.aterehov.gen.ai.plugin;

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

public class JSONFormatPlugin {

    private static final int MAX_TOKENS = 1024;

    private static final double TEMPERATURE = 0.3;

    private static final double TOP_P = 0.5;

    public static final String JSON_FORMATTING = """
            {{$INPUT}}. Response should be in the following format (JSON), no other symbols allowed:\
             {
                "response": {Generated Response}
            }
            """.stripIndent();

    private final KernelFunction<String> jsonFormatFunction;

    public JSONFormatPlugin() {
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
    }

    @DefineKernelFunction(description = "Any input given, return response in JSON format.",
            name = "jsonFormat", returnType = "java.lang.String")
    public Mono<String> jsonFormat(
            @KernelFunctionParameter(description = "Any input.", name = "input") String input,
            Kernel kernel) {
        return processAsync(this.jsonFormatFunction, input, kernel);
    }

    private static Mono<String> processAsync(KernelFunction<String> func, String input,
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


}
