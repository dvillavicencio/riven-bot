package com.deahtstroke.rivenbot.service;

import com.deahtstroke.rivenbot.mapper.PGCRMapper;
import com.deahtstroke.rivenbot.repository.PGCRRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;

@ExtendWith(MockitoExtension.class)
class PostGameCarnageReportServiceTest {

  @Mock
  WebClient.Builder builder;

  @Mock
  PGCRMapper mapper;

  @Mock
  PGCRRepository pgcrRepository;

  @InjectMocks
  PostGameCarnageService sut;

  @Test
  @DisplayName("WebClient has correct parameters")
  void webclientHasCorrectParameters() {
    Assertions.assertThat()
  }
}
