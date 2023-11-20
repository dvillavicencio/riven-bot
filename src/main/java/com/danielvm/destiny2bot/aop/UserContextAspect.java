package com.danielvm.destiny2bot.aop;

import com.danielvm.destiny2bot.context.UserIdentity;
import com.danielvm.destiny2bot.context.UserIdentityContext;
import com.danielvm.destiny2bot.dto.discord.interactions.Interaction;
import com.danielvm.destiny2bot.enums.InteractionType;
import com.danielvm.destiny2bot.exception.UserIdentityNotFoundException;
import com.danielvm.destiny2bot.repository.UserDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Aspect
@Component
public class UserContextAspect {

    private final UserDetailsRepository userDetailsRepository;

    public UserContextAspect(UserDetailsRepository userDetailsRepository) {
        this.userDetailsRepository = userDetailsRepository;
    }

    /**
     * AOP advice to populate user identity data based on database entry
     *
     * @param interaction The interaction sent by Discord
     */
    @Before(value = "execution(* com.danielvm.destiny2bot.service.InteractionService.handleInteraction(..)) " +
            "&& args(interaction)")
    public void userContextAdvice(Interaction interaction) throws UserIdentityNotFoundException {
        var userDiscordId = interaction.getUser().getId();
        if (Objects.equals(InteractionType.PING.getType(), interaction.getType())) {
            log.debug("Interaction received was of type PING. No user identity context required");
            return;
        }
        if (userDetailsRepository.existsByDiscordId(userDiscordId)) {
            UserIdentityContext.setUserIdentity(new UserIdentity(userDiscordId));
        } else {
            throw new UserIdentityNotFoundException(
                    "User identity not found for Discord Id [%s]".formatted(userDiscordId));
        }
    }
}
