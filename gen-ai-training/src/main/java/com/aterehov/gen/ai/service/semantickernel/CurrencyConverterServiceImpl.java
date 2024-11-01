package com.aterehov.gen.ai.service.semantickernel;

import com.aterehov.gen.ai.domain.Currency;
import com.aterehov.gen.ai.service.CurrencyConverterService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrencyConverterServiceImpl implements CurrencyConverterService {

    @Override
    public double convert(Currency convertFrom, Currency convertTo, double amount) {
        log.info("Converting {} from {} to {}", amount, convertFrom, convertTo);
        double amountInUSD = amount / convertFrom.getRateToUSD();
        return amountInUSD * convertTo.getRateToUSD();
    }
}
