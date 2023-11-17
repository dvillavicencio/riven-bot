package com.danielvm.destiny2bot.dto.discord.interactions;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Embedded {

    private String title;

    private String type;

    private String description;

    private String url;

    private String timestamp;

    private Integer color;

    private Object footer;

    private EmbeddedImage image;

    private Object thumbnail;

    private Object video;

    private Object provider;

    private Object author;

    private List<EmbeddedField> fields;
}
