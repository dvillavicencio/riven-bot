package com.danielvm.destiny2bot.dto.destiny.manifest;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ManifestDto {

    @JsonAlias("Response")
    private GenericResponseFields response;

}
