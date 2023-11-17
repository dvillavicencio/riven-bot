package com.danielvm.destiny2bot.enums;

import lombok.Getter;

public enum InteractionType {
    PING(1),
    APPLICATION_COMMAND(2),
    MESSAGE_COMPONENT(3),
    APPLICATION_COMMAND_AUTOCOMPLETE(4),
    MODAL_SUBMIT(5);

    @Getter
    private final Integer type;

    InteractionType(Integer type) {
        this.type = type;
    }
}
