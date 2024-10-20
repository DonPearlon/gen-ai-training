package com.aterehov.gen.ai.plugin;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.semanticfunctions.KernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import reactor.core.publisher.Mono;

public class ConversationSummaryPlugin extends AbstractKernelPlugin {
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

    private KernelFunction<String> summarizeConversationFunction;

    public ConversationSummaryPlugin() {
        this.summarizeConversationFunction = KernelFunction
                .<String>createFromPrompt(SUMMARIZE_CONVERSATION_PROMPT)
                .withDefaultExecutionSettings(createDefaultPromptExecutionSettings())
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


}
