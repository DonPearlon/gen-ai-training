package com.aterehov.gen.ai.domain;

public enum Currency {

    USD(1.0),
    EUR(0.9201),
    BYN(3.3162),
    RUB(96.8177),
    PLN(4.0061),
    CNY(7.0977);

    private final double rateToUSD;

    Currency(double rateToUSD) {
        this.rateToUSD = rateToUSD;
    }

    public double getRateToUSD() {
        return this.rateToUSD;
    }
}
