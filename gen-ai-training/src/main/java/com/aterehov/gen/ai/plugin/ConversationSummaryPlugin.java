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

public class ConversationSummaryPlugin {
    public static final String SUMMARIZE_CONVERSATION_PROMPT = """
        BEGIN CONTENT TO SUMMARIZE:
        {{$INPUT}}

        END CONTENT TO SUMMARIZE.

        Summarize the conversation in 'CONTENT TO SUMMARIZE', identifying main points of discussion and any conclusions that were reached.
        Do not incorporate other general knowledge.
        Summary is in plain text, in complete sentences, with no markup or tags.

        BEGIN SUMMARY:
        """
            .stripIndent();

    private static final int MAX_TOKENS = 1024;

    private final KernelFunction<String> summarizeConversationFunction;

    public ConversationSummaryPlugin(PromptExecutionSettings promptExecutionSettings) {
        this.summarizeConversationFunction = KernelFunction
                .<String>createFromPrompt(SUMMARIZE_CONVERSATION_PROMPT)
                .withDefaultExecutionSettings(promptExecutionSettings)
                .withName("summarizeConversation")
                .withDescription(
                        "Summarize conversation based on chat history.")
                .build();
    }

    @DefineKernelFunction(
            description = "Given a chat history, summarize the conversation.",
            name = "summarizeConversation",
            returnType = "java.lang.String")
    public Mono<String> SummarizeConversationAsync(
            @KernelFunctionParameter(description = "Chat history.", name = "input") String input,
            Kernel kernel) {
        return processAsync(this.summarizeConversationFunction, input, kernel);
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
