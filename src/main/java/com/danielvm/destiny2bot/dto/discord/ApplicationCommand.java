package com.danielvm.destiny2bot.dto.discord;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class ApplicationCommand {

    private Object id;

    private Integer type;

    @JsonAlias("application_id")
    private String applicationId;

    private String name;

}
