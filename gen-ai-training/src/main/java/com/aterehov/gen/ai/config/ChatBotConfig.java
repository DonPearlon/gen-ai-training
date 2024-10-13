package com.aterehov.gen.ai.config;


import com.aterehov.gen.ai.plugin.ChatBotResponseFormatPlugin;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
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

    @Value("${client-azureopenai-top-p:0.5}")
    private double topP;

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
    public KernelPlugin chatBotResponseFormatPlugin() {
        return KernelPluginFactory.createFromObject(new ChatBotResponseFormatPlugin(),
                "ChatBotResponseFormatPlugin");
    }

    @Bean
    public Kernel kernel(ChatCompletionService chatCompletionService, KernelPlugin chatBotResponseFormatPlugin) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(chatBotResponseFormatPlugin)
                .build();
    }

    @Bean
    public InvocationContext invocationContext() {
        var executionSettings = PromptExecutionSettings.builder()
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .withTopP(topP)
                .build();
        return new InvocationContext.Builder()
                .withPromptExecutionSettings(executionSettings)
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }
}
