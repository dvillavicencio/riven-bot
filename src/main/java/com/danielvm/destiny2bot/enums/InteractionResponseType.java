package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum InteractionResponseType {

    PONG(1),
    CHANNEL_MESSAGE_WITH_SOURCE(4),
    DEFERRED_CHANNEL_MESSAGE_WITH_SOURCE(5),
    DEFERRED_UPDATE_MESSAGE(6),
    UPDATE_MESSAGE(7),
    APPLICATION_COMMAND_AUTOCOMPLETE_RESULT(8),
    MODAL(9),
    PREMIUM_REQUIRED(10);

    @Getter
    private final Integer type;

    InteractionResponseType(Integer type) {
        this.type = type;
    }
}
