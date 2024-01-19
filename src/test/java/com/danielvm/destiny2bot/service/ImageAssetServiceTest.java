package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.Option;
import java.io.IOException;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class ImageAssetServiceTest {

  @InjectMocks
  private ImageAssetService sut;

  @Test
  @DisplayName("Retrieve encounter images' cache works as intended")
  public void retrieveEncounterImagesCachesSuccessfully() throws IOException {
    // given: an interaction
    List<Option> options = List.of(
        new Option("raid", 3, "last_wish", false),
        new Option("encounter", 3, "kalli", false));
    InteractionData data = InteractionData.builder()
        .options(options)
        .build();
    Interaction interaction = Interaction.builder().data(data).build();

    // when: retrieveEncounterImages is called
    var response = StepVerifier.create(sut.retrieveEncounterImages(interaction));

    // then: the response is correct
    response.assertNext(map -> {
          Assertions.assertThat(map.get(0L))
              .isEqualTo(new ClassPathResource("classpath:static/raids/last_wish/kalli/kalli.jpeg"));
        })
        .verifyComplete();

    // and: calling it again with different parameters does not use the cache value
    List<Option> newOptions = List.of(
        new Option("raid", 3, "last_wish", false),
        new Option("encounter", 3, "shuro_chi", false));
    InteractionData newData = InteractionData.builder().options(newOptions).build();
    Interaction newInteraction = Interaction.builder().data(newData).build();
    StepVerifier.create(sut.retrieveEncounterImages(newInteraction))
        .assertNext(map -> {
          Assertions.assertThat(map.get(0L))
              .isEqualTo(new ClassPathResource(
                  "classpath:static/raids/last_wish/shuro_chi/shuro_chi.jpeg"));
        })
        .verifyComplete();
  }

}
