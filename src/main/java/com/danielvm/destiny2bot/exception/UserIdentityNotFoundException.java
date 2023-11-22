package com.danielvm.destiny2bot.exception;

import java.io.Serial;

public class UserIdentityNotFoundException extends Throwable {
    @Serial
    private static final long serialVersionUID = 4099942957103917770L;

    private String message;

    public UserIdentityNotFoundException(String message) {
        super(message);
        this.message = message;
    }
}