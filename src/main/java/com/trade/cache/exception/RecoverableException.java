package com.trade.cache.exception;

public class RecoverableException extends RuntimeException {
    public RecoverableException(Throwable cause) {
        super(cause);
    }
}
