package com.aterehov.gen.ai.plugin;

import com.aterehov.gen.ai.domain.Currency;
import com.aterehov.gen.ai.service.CurrencyConverterService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
public class CurrencyConverterPlugin {
    private final CurrencyConverterService currencyConverterService;

    @DefineKernelFunction(
            name = "convert_currency",
            description = "Convert amount of money from one currency to another",
            returnDescription = "Returns amount of money converted from one currency to another",
            returnType = "java.lang.Double")
    public Mono<Double> convert(
            @KernelFunctionParameter(name = "convertFrom",
                    description = "Currency from which money should be converted",
                    type = com.aterehov.gen.ai.domain.Currency.class)
            Currency convertFrom,
            @KernelFunctionParameter(name = "convertTo",
                    description = "Currency to which money should be converted",
                    type = com.aterehov.gen.ai.domain.Currency.class)
            Currency convertTo,
            @KernelFunctionParameter(name = "amount", description = "The amount of money to convert", type = java.lang.Double.class)
            Double amount

    ) {
        log.info("calling convert function of the CurrencyConverterPlugin");

        return Mono.just(currencyConverterService.convert(convertFrom, convertTo, amount));
    }
}
