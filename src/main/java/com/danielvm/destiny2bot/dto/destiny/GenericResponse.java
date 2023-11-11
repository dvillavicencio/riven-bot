package com.danielvm.destiny2bot.dto.destiny;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class GenericResponse<T> {

    @JsonAlias("Response")
    @Nullable
    private T response;
}
