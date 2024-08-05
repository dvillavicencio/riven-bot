package com.deahtstroke.rivenbot.factory;

import com.deahtstroke.rivenbot.dto.discord.Interaction;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import reactor.core.publisher.Mono;

public interface Factory<T> {

  Mono<InteractionResponse> serve(T data, Interaction interaction);
}
