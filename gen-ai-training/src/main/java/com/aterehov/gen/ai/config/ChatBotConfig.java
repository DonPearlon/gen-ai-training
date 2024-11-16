package com.aterehov.gen.ai.config;

import com.aterehov.gen.ai.domain.Currency;
import com.aterehov.gen.ai.dto.VectorDbRequest;
import com.aterehov.gen.ai.dto.VectorDbResponse;
import com.aterehov.gen.ai.plugin.AgePlugin;
import com.aterehov.gen.ai.plugin.ConversationSummaryPlugin;
import com.aterehov.gen.ai.plugin.CurrencyConverterPlugin;
import com.aterehov.gen.ai.plugin.ProductReviewPlugin;
import com.aterehov.gen.ai.service.VectorDbService;
import com.aterehov.gen.ai.service.impl.CurrencyConverterServiceImpl;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.InvocationReturnMode;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfig {

    @Value("${client-azureopenai-deployment-name}")
    private String deploymentName;

    @Value("${client-azureopenai-temperature:0.5}")
    private double temperature;

    @Value("${client-azureopenai-max-tokens:1024}")
    private int maxTokens;

    @Value("${client-azureopenai-top-p:1}")
    private double topP;

    @Value("${client-azureopenai-frequency-penalty:2}")
    private double frequencyPenalty;

    @Autowired
    private VectorDbService vectorDbService;

    @Bean
    public ChatCompletionService chatCompletionService(OpenAIAsyncClient openAIClient) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIClient)
                .build();
    }

    @Bean
    public KernelPlugin agePlugin() {
        return KernelPluginFactory.createFromObject(new AgePlugin(),
                "AgePlugin");
    }

    @Bean
    public KernelPlugin currencyConverterPlugin() {
        return KernelPluginFactory.createFromObject(new CurrencyConverterPlugin(new CurrencyConverterServiceImpl()),
                "CurrencyConverterPlugin");
    }

    @Bean
    public KernelPlugin conversationSummaryPlugin() {
        return KernelPluginFactory.createFromObject(new ConversationSummaryPlugin(executionSettings()),
                "ConversationSummaryPlugin");
    }

    @Bean
    public KernelPlugin productReviewPlugin() {
        return KernelPluginFactory.createFromObject(new ProductReviewPlugin(vectorDbService),
                "ProductReviewPlugin");
    }

    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService) {

        var currencyConverter = new ContextVariableTypeConverter<>(Currency.class,
                obj -> Currency.valueOf(obj.toString()), Enum::toString, Currency::valueOf);

        var vectorDbRequestConverter = new ContextVariableTypeConverter<>(VectorDbRequest.class,
                obj -> new VectorDbRequest(obj.toString()), VectorDbRequest::text, VectorDbRequest::new);

        var vectorDbResponseConverter = new ContextVariableTypeConverter<>(VectorDbResponse.class,
                obj -> new VectorDbResponse(obj.toString(), null), VectorDbResponse::toString, response -> new VectorDbResponse(response, null));

        ContextVariableTypes
                .addGlobalConverter(currencyConverter);

        ContextVariableTypes
                .addGlobalConverter(vectorDbRequestConverter);

        ContextVariableTypes
                .addGlobalConverter(vectorDbResponseConverter);

        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(agePlugin())
                .withPlugin(currencyConverterPlugin())
                .withPlugin(conversationSummaryPlugin())
                .withPlugin(productReviewPlugin())
                .build();
    }

    @Bean
    public PromptExecutionSettings executionSettings() {
        return PromptExecutionSettings.builder()
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .withTopP(topP)
                .withFrequencyPenalty(frequencyPenalty)
                .build();
    }

    @Bean
    public InvocationContext invocationContext(PromptExecutionSettings executionSettings) {
        return new InvocationContext.Builder()
                .withPromptExecutionSettings(executionSettings)
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }
}
