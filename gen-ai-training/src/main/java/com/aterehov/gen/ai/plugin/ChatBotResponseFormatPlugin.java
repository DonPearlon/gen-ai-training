package com.aterehov.gen.ai.plugin;

import com.aterehov.gen.ai.dto.ChatBotResponse;
import com.google.gson.Gson;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import reactor.core.publisher.Mono;

public class ChatBotResponseFormatPlugin extends AbstractKernelPlugin {
    private final Gson gson = new Gson();

    @DefineKernelFunction(description = "Return result in form of chat bot response object string",
            name = "responseObject", returnType = "java.lang.String")
    public Mono<String> responseObject(
            @KernelFunctionParameter(description = "Only input to this particular function.", name = "input") String input) {
        return Mono.just(gson.toJson(new ChatBotResponse(input)));
    }
}
