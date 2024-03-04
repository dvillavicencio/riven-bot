package com.danielvm.destiny2bot.service;

import static com.danielvm.destiny2bot.util.InteractionUtil.retrieveInteractionOption;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.exception.ImageProcessingException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
public class RaidInfographicsService {

  private static final String RAID_OPTION_NAME = "raid";
  private static final String ENCOUNTER_OPTION_NAME = "encounter";
  private static final String ASSETS_BASE_PATH = "classpath:static/raids/%s/%s/*.*";

  private final PathMatchingResourcePatternResolver resourcePatternResolver;

  public RaidInfographicsService(PathMatchingResourcePatternResolver resourcePatternResolver) {
    this.resourcePatternResolver = resourcePatternResolver;
  }

  /**
   * Retrieve all images for all encounters given a raid name and an encounter name
   *
   * @param interaction The interaction from where to extract raid and encounter info
   * @return HashMap with an indexed key and a value of the corresponding classpath resource
   * @throws IOException In case something unexpected happens when retrieving the files in memory
   */
  public Mono<Map<Long, Resource>> retrieveEncounterImages(Interaction interaction)
      throws IOException {
    List<Option> options = interaction.getData().getOptions();
    String raidDirectory = retrieveInteractionOption(options, RAID_OPTION_NAME);
    String encounterDirectory = retrieveInteractionOption(options, ENCOUNTER_OPTION_NAME);

    String basePath = ASSETS_BASE_PATH.formatted(raidDirectory, encounterDirectory);
    Resource[] resources;
    try {
      resources = resourcePatternResolver.getResources(basePath);
    } catch (IOException e) {
      log.error("Something unexpected happened when fetching [{}]", basePath, e);
      throw new ImageProcessingException(
          "Something unexpected happened when fetching resources for raid [%s] and encounter [%s]".formatted(
              raidDirectory, encounterDirectory), e);
    }
    return Flux.fromArray(resources)
        .index()
        .collectMap(Tuple2::getT1, Tuple2::getT2);
  }

}
