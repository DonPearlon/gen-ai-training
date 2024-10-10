package com.aterehov.gen.ai.config;


import com.aterehov.gen.ai.plugin.ChatBotResponseFormatPlugin;
import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
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
}
