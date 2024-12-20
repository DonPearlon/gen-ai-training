package com.aterehov.gen.ai.config;

import com.aterehov.gen.ai.domain.Currency;
import com.aterehov.gen.ai.plugin.AgePlugin;
import com.aterehov.gen.ai.plugin.ConversationSummaryPlugin;
import com.aterehov.gen.ai.plugin.CurrencyConverterPlugin;
import com.aterehov.gen.ai.service.semantickernel.CurrencyConverterServiceImpl;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatBotConfig {

    @Value("${client-azureopenai-key}")
    private String apiKey;

    @Value("${client-azureopenai-endpoint}")
    private String endpoint;

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

    @Bean
    public OpenAIAsyncClient openAIClient() {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(apiKey))
                .endpoint(endpoint)
                .buildAsyncClient();
    }

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
    public Kernel kernel(ChatCompletionService chatCompletionService) {

        var currencyConverter = new ContextVariableTypeConverter<>(Currency.class,
                obj -> Currency.valueOf(obj.toString()), Enum::toString, Currency::valueOf);

        ContextVariableTypes
                .addGlobalConverter(currencyConverter);

        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(agePlugin())
                .withPlugin(currencyConverterPlugin())
                .withPlugin(conversationSummaryPlugin())
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
