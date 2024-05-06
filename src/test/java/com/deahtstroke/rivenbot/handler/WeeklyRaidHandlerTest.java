package com.deahtstroke.rivenbot.handler;

import static org.mockito.Mockito.when;

import com.deahtstroke.rivenbot.dto.WeeklyActivity;
import com.deahtstroke.rivenbot.enums.ActivityMode;
import com.deahtstroke.rivenbot.enums.InteractionResponseType;
import com.deahtstroke.rivenbot.service.WeeklyActivitiesService;
import com.deahtstroke.rivenbot.util.MessageUtils;
import com.deahtstroke.rivenbot.dto.discord.InteractionResponse;
import java.time.ZonedDateTime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifier.FirstStep;

@ExtendWith(MockitoExtension.class)
public class WeeklyRaidHandlerTest {

  @Mock
  WeeklyActivitiesService weeklyActivitiesService;

  @InjectMocks
  WeeklyRaidHandler sut;

  @Test
  @DisplayName("Create message is successful")
  public void createMessageIsSuccessful() {
    WeeklyActivity weeklyActivity = new WeeklyActivity(
        "dungeon", "description", ZonedDateTime.now(), ZonedDateTime.now());
    when(weeklyActivitiesService.getWeeklyActivity(ActivityMode.RAID))
        .thenReturn(Mono.just(weeklyActivity));

    // when: create message is called
    FirstStep<InteractionResponse> response = StepVerifier.create(
        sut.createResponse(null));

    // then: the message created is correct
    String expectedMessage = WeeklyRaidHandler.MESSAGE_TEMPLATE.formatted(
        weeklyActivity.getName(),
        MessageUtils.formatDate(weeklyActivity.getEndDate().toLocalDate()));
    response
        .assertNext(ir -> {
          Assertions.assertThat(ir.getType())
              .isEqualTo(InteractionResponseType.CHANNEL_MESSAGE_WITH_SOURCE.getType());
          Assertions.assertThat(ir.getData().getContent())
              .isEqualTo(expectedMessage);
        }).verifyComplete();
  }
}
