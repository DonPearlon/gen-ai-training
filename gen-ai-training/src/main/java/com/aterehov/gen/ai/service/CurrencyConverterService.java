package com.aterehov.gen.ai.service;

import com.aterehov.gen.ai.domain.Currency;

public interface CurrencyConverterService {
    double convert(Currency convertFrom, Currency convertTo, double amount);
}
