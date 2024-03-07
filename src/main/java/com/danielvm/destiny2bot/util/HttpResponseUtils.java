package com.danielvm.destiny2bot.util;

import com.danielvm.destiny2bot.dto.discord.InteractionResponse;
import com.danielvm.destiny2bot.exception.ImageProcessingException;
import java.io.IOException;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;

/**
 * Utilities for reformatting Request/Response entities
 */
@Slf4j
public class HttpResponseUtils {

  private static final String JSON_PAYLOAD_HEADER = "payload_json";
  private static final String JSON_PAYLOAD_CONTENT_DISPOSITION_VALUE = "form-data; name=\"payload_json\"";
  private static final String FILE_PAYLOAD_INDEX = "files[%s]";

  private HttpResponseUtils() {
  }

  /**
   * Utility method to create a multi-part form response that supports adding files in an indexed
   * manner. Discord itself requires files to be indexed in a specific way and be sent in a
   * multi-part form response which is not something that's common. This utility method helps
   * alleviate that.
   *
   * @param interactionResponse The interactionResponse that will be sent as a Json Payload
   * @param resources           The Map indexed classpath resources
   * @return MultiValueMap HttpEntities with all files and the json payload
   */
  public static MultiValueMap<String, HttpEntity<?>> filesResponse(
      InteractionResponse interactionResponse, Map<Long, Resource> resources) {
    MultipartBodyBuilder builder = new MultipartBodyBuilder();
    buildJson(interactionResponse, builder);
    buildFiles(resources, builder);
    return builder.build();
  }

  private static void buildFiles(Map<Long, Resource> resources,
      MultipartBodyBuilder builder) {
    resources.forEach((fileIndex, resource) -> {
      try {
        String fileExtension = FilenameUtils.getExtension(resource.getFilename());
        if (fileExtension != null) {
          builder.part(FILE_PAYLOAD_INDEX.formatted(fileIndex), resource.getContentAsByteArray())
              .contentType(new MediaType("image", fileExtension))
              .filename(resource.getFilename());
        }
      } catch (IOException e) {
        throw new ImageProcessingException(
            "Unable to find image at index [%s]".formatted(fileIndex), e);
      }
    });
  }

  private static void buildJson(InteractionResponse interactionResponse,
      MultipartBodyBuilder builder) {
    builder.part(JSON_PAYLOAD_HEADER, interactionResponse)
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_DISPOSITION, JSON_PAYLOAD_CONTENT_DISPOSITION_VALUE);
  }

}
