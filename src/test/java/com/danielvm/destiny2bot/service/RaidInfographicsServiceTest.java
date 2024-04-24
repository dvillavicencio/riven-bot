package com.danielvm.destiny2bot.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.danielvm.destiny2bot.TestUtils;
import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.InteractionData;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.exception.ImageProcessingException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class RaidInfographicsServiceTest {

  @Mock
  private PathMatchingResourcePatternResolver resourcePatternResolver;

  @InjectMocks
  private RaidInfographicsService sut;

  @Test
  @DisplayName("Retrieve encounter images' works as intended")
  public void retrieveEncounterImagesIsSuccessful() throws IOException {
    // given: an interaction
    String raidName = "last_wish";
    String raidEncounter = "kalli";
    List<Option> options = List.of(
        new Option("raid", 3, raidName, false, Collections.emptyList()),
        new Option("encounter", 3, raidEncounter, false, Collections.emptyList()));
    InteractionData data = InteractionData.builder()
        .options(options)
        .build();
    Interaction interaction = Interaction.builder().data(data).build();

    String basePath = "classpath:static/raids/%s/%s/*.*".formatted(raidName, raidEncounter);
    Resource mockResource1 = TestUtils.createResourceWithContent("Hello");
    Resource mockResource2 = TestUtils.createResourceWithContent("World");
    Resource[] resources = new Resource[]{mockResource1, mockResource2};

    when(resourcePatternResolver.getResources(basePath))
        .thenReturn(resources);

    // when: retrieveEncounterImages is called
    var response = StepVerifier.create(sut.retrieveEncounterImages(interaction));

    // then: the response is correct
    response.assertNext(map -> {
          try {
            assertThat(new String(map.get(0L).getInputStream().readAllBytes())).isEqualTo("Hello");
            assertThat(new String(map.get(1L).getInputStream().readAllBytes())).isEqualTo("World");
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        })
        .verifyComplete();
  }

  @Test
  @DisplayName("Retrieve encounter images' works as intended")
  public void retrieveEncountersImagesFailsOnIOException() throws IOException {
    // given: an interaction
    String raidName = "last_wish";
    String raidEncounter = "kalli";
    List<Option> options = List.of(
        new Option("raid", 3, raidName, false, Collections.emptyList()),
        new Option("encounter", 3, raidEncounter, false, Collections.emptyList()));
    InteractionData data = InteractionData.builder()
        .options(options)
        .build();
    Interaction interaction = Interaction.builder().data(data).build();

    String basePath = "classpath:static/raids/%s/%s/*.*".formatted(raidName, raidEncounter);

    when(resourcePatternResolver.getResources(basePath))
        .thenThrow(new IOException("Some IO Exception"));

    // when: retrieveEncounterImages is called
    Assertions.assertThatThrownBy(() -> sut.retrieveEncounterImages(interaction).block())
        .isInstanceOf(ImageProcessingException.class);

    Assertions.assertThatThrownBy(() -> sut.retrieveEncounterImages(interaction).block())
        .hasMessage(
            "Something unexpected happened when fetching resources for raid [%s] and encounter [%s]",
            raidName, raidEncounter);


  }
}
