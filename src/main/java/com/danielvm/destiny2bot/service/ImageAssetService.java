package com.danielvm.destiny2bot.service;

import com.danielvm.destiny2bot.dto.discord.Interaction;
import com.danielvm.destiny2bot.dto.discord.Option;
import com.danielvm.destiny2bot.exception.BadRequestException;
import com.danielvm.destiny2bot.exception.InternalServerException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

@Service
@Slf4j
public class ImageAssetService {

  private static final String RAID_OPTION_NAME = "raid";
  private static final String ENCOUNTER_OPTION_NAME = "encounter";
  private static final String ASSETS_BASE_PATH = "static/raids/%s/%s/*.*";

  private static String retrieveInteractionOption(List<Option> options, String optionName) {
    return String.valueOf(options.stream()
        .filter(o -> Objects.equals(optionName, o.getName())).findAny()
        .orElseThrow(() -> new BadRequestException("No raid option present in the request",
            HttpStatus.BAD_REQUEST)).getValue());
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
      resources = new PathMatchingResourcePatternResolver().getResources(
          "classpath:" + basePath);
    } catch (IOException e) {
      log.error("Something unexpected happened when fetching [{}]", basePath, e);
      throw new InternalServerException(
          "Something unexpected happened when fetching resources for raid [%s] and encounter [%s]".formatted(
              raidDirectory, encounterDirectory), HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    return Flux.fromArray(resources)
        .index()
        .collectMap(Tuple2::getT1, Tuple2::getT2);
  }

}