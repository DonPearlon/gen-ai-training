package com.aterehov.gen.ai.plugin;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.KernelFunctionArguments;
import com.microsoft.semantickernel.text.TextChunker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public class AbstractKernelPlugin {

    protected static final int MAX_TOKENS = 1024;

    protected static final double TEMPERATURE = 0.7;

    protected static final double TOP_P = 1;

    protected static PromptExecutionSettings createDefaultPromptExecutionSettings() {
        return PromptExecutionSettings.builder()
                .withMaxTokens(MAX_TOKENS)
                .withTemperature(TEMPERATURE)
                .withTopP(TOP_P)
                .build();
    }

    protected static Mono<String> processAsync(KernelFunction<String> func, String input,
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
