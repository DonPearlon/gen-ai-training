package com.aterehov.gen.ai.dto;

public record ChatBotResponse(String output) {
    @Override
    public String toString() {
        return "{\"output\":\"" + output + "\"}";
    }
}
