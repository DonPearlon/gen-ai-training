package com.aterehov.gen.ai.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Slf4j
public class AgePlugin {

    public static final String DATE_FORMAT = "dd-MM-yyyy";

    @DefineKernelFunction(
            name = "calculate_age",
            description = "Calculate age from birth date string",
            returnDescription = "Returns age calculated from birth date string",
            returnType = "java.lang.String")
    public Mono<String> calculateAge(
            @KernelFunctionParameter(name = "birthDateStr")
            String birthDateStr

    ) {
        log.info("calling calculateAge function of the AgePlugin");

        try {
            var formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);

            var birthDate = LocalDate.parse(birthDateStr, formatter);

            var currentDate = LocalDate.now();

            var age = Period.between(birthDate, currentDate);

            return Mono.just(String.valueOf(age.getYears()));
        } catch (Exception exception) {
            return Mono.just("Failed to calculate age. Birth date should be in %s format".formatted(DATE_FORMAT));
        }
    }
}
