package com.danielvm.destiny2bot.dto.destiny;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.annotation.Nullable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericResponse<T> {

    @JsonAlias("Response")
    @Nullable
    private T response;
}
