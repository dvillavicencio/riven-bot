package com.danielvm.destiny2bot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document
@Builder
public class UserDetails {

    @Id
    public String id;

    public String discordUsername;

    public String discordId;

    public String accessToken;

    public String refreshToken;

    public Long expiresIn;
}
