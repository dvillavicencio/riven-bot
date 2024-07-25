package com.deahtstroke.rivenbot.factory;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.enums.MessageComponentId;
import com.deahtstroke.rivenbot.exception.NoSuchHandlerException;
import com.deahtstroke.rivenbot.handler.MessageComponentSource;
import com.deahtstroke.rivenbot.handler.RaidStatsButtonHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageComponentFactoryTest {

  @Mock
  RaidStatsButtonHandler raidStatsButtonHandler;

  @InjectMocks
  MessageComponentFactory sut;

  @Test
  @DisplayName("Get message component handler is successful for valid component IDs")
  void shouldReturnHandlerSuccessfully() {
    // given: a componentID
    MessageComponentId componentId = MessageComponentId.RAID_STATS_COMPREHENSION;

    // when: getHandler is invoked
    MessageComponentSource handler = sut.getHandler(componentId);

    // then: the handler is of the correct instance
    assertThat(handler).isInstanceOf(raidStatsButtonHandler.getClass());
  }

  @Test
  @DisplayName("Get message component handler fails for invalid component IDs")
  void shouldThrowErrorForReturnHandler() {
    // given: a componentID without a handler
    MessageComponentId componentId = MessageComponentId.MESSAGE_COMPONENT_TEST;

    // when: getHandler is invoked
    // then: an exception is thrown with the correct error message
    Assertions.assertThrows(NoSuchHandlerException.class, () -> sut.getHandler(componentId),
        "No handler found for component [%s] with ID [%s]".formatted(componentId,
            componentId.getId()));
  }
}
