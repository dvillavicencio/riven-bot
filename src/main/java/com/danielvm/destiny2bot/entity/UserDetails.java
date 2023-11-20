package com.danielvm.destiny2bot.entity;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Document
@Builder
public class UserDetails {

    @Id
    private String id;

    private String discordUsername;

    private String discordId;

    private String accessToken;

    private String refreshToken;

    private Instant expiration;
}
