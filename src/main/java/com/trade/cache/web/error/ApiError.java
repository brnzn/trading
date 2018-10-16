package com.trade.cache.web.error;

public class ApiError {

    private final String message;

    ApiError(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
