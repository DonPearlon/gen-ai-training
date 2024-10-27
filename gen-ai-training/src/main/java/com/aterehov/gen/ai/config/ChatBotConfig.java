package com.aterehov.gen.ai.config;

import com.aterehov.gen.ai.plugin.ConversationSummaryPlugin;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class ChatBotConfig {

    @Value("${client-azureopenai-key}")
    private String apiKey;

    @Value("${client-azureopenai-endpoint}")
    private String endpoint;

    @Value("${client-azureopenai-deployment-names}")
    private String deploymentNames;

    @Value("${client-azureopenai-temperature:0.5}")
    private double temperature;

    @Value("${client-azureopenai-max-tokens:1024}")
    private int maxTokens;

    @Value("${client-azureopenai-top-p:0.5}")
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
    public Map<String, ChatCompletionService> modelToChatCompletionService(OpenAIAsyncClient openAIClient) {
        Map<String, ChatCompletionService> modelToChatCompletionService = new HashMap<>();
        Arrays.stream(deploymentNames.split(","))
                .forEach(deploymentName ->
                        modelToChatCompletionService.put(deploymentName, createChatCompletionService(openAIClient, deploymentName)));
        return modelToChatCompletionService;
    }

    public ChatCompletionService createChatCompletionService(OpenAIAsyncClient openAIClient, String deploymentName) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIClient)
                .build();
    }

    @Bean
    public Map<String, Kernel> modelToKernel(Map<String, ChatCompletionService> modelToChatCompletionService) {
        Map<String, Kernel> modelToKernel = new HashMap<>();
        Arrays.stream(deploymentNames.split(","))
                .forEach(deploymentName ->
                       modelToKernel.put(deploymentName, createKernel(modelToChatCompletionService.get(deploymentName))));
        return modelToKernel;
    }

    @Bean
    public KernelPlugin conversationSummaryPlugin(PromptExecutionSettings promptExecutionSettings) {
        return KernelPluginFactory.createFromObject(new ConversationSummaryPlugin(promptExecutionSettings),
                "ConversationSummaryPlugin");
    }



    public Kernel createKernel(ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(conversationSummaryPlugin(defaultPromptExecutionSettings()))
                .build();
    }

    @Bean
    public PromptExecutionSettings defaultPromptExecutionSettings() {
        return PromptExecutionSettings.builder()
                .withTemperature(temperature)
                .withMaxTokens(maxTokens)
                .withTopP(topP)
                .withFrequencyPenalty(frequencyPenalty)
                .build();
    }

    private Map<String, InvocationContext> invocationContextMap() {
        Map<String, InvocationContext> invocationContextMap = new HashMap<>();
        invocationContextMap.put(null, defaultInvocationContext());
        invocationContextMap.put("default", defaultInvocationContext());
        invocationContextMap.put("precise", preciseInvocationContext());
        invocationContextMap.put("neutral", neutralInvocationContext());
        invocationContextMap.put("creative", creativeInvocationContext());
        return invocationContextMap;
    }

    public InvocationContext defaultInvocationContext() {
        return createInvocationContext(defaultPromptExecutionSettings());
    }

    public InvocationContext preciseInvocationContext() {
        var promptExecutionSettings = PromptExecutionSettings.builder()
                .withTemperature(0.2)
                .withMaxTokens(maxTokens)
                .withTopP(0.5)
                .withFrequencyPenalty(0.8)
                .build();
        return createInvocationContext(promptExecutionSettings);
    }

    public InvocationContext neutralInvocationContext() {
        var promptExecutionSettings = PromptExecutionSettings.builder()
                .withTemperature(0.5)
                .withMaxTokens(maxTokens)
                .withTopP(1.0)
                .withFrequencyPenalty(0.5)
                .build();
        return createInvocationContext(promptExecutionSettings);
    }

    public InvocationContext creativeInvocationContext() {
        var promptExecutionSettings = PromptExecutionSettings.builder()
                .withTemperature(0.9)
                .withMaxTokens(maxTokens)
                .withTopP(1.0)
                .withFrequencyPenalty(0.2)
                .build();
        return createInvocationContext(promptExecutionSettings);
    }

    private InvocationContext createInvocationContext(PromptExecutionSettings promptExecutionSettings) {
        return new InvocationContext.Builder()
                .withPromptExecutionSettings(promptExecutionSettings)
                .withReturnMode(InvocationReturnMode.LAST_MESSAGE_ONLY)
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
                .build();
    }

}
