package com.danielvm.destiny2bot;

import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.mockito.Mockito;
import org.springframework.core.io.Resource;

public class TestUtils {

  private TestUtils() {}

  /**
   * Creates an instance of a Resource ready to be mocked
   *
   * @param content The content to be mocked
   * @return The mocked resources
   * @throws IOException Whenever something goes wrong
   */
  public static Resource createResourceWithContent(String content) throws IOException {
    Resource mockedResource = Mockito.mock(Resource.class);
    when(mockedResource.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes(
        StandardCharsets.UTF_8)));
    return mockedResource;
  }

  /**
   * Creates an instance of a Resource
   *
   * @param filename The filename of the created resource
   * @return The mocked resource
   */
  public static Resource createResourceWithName(String filename) throws IOException {
    Resource mockedResource = Mockito.mock(Resource.class);
    when(mockedResource.getFilename()).thenReturn(filename);
    when(mockedResource.contentLength()).thenReturn(16L);
    return mockedResource;
  }

}
