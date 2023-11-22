package com.danielvm.destiny2bot.exception;

import java.io.Serial;

public class ResourceNotFoundException extends RuntimeException {
    @Serial
    private static final long serialVersionUID = -4367872183871777447L;

    private String message;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
