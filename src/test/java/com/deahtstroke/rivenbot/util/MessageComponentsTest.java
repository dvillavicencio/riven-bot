package com.deahtstroke.rivenbot.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.dto.discord.MessageComponent;
import com.deahtstroke.rivenbot.entity.ButtonStyle;
import com.deahtstroke.rivenbot.enums.MessageComponentId;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageComponentsTest {

  static String createRandomString(int length) {
    String alphaNumeric = """
        ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz""";
    StringBuilder illegalButtonId = new StringBuilder();
    for (int i = 0; i < length; i++) {
      illegalButtonId.append(alphaNumeric.charAt((int) (alphaNumeric.length() * Math.random())));
    }
    return illegalButtonId.toString();
  }

  static Stream<Arguments> illegalLinkButtonArguments() {
    return Stream.of(
        Arguments.of(createRandomString(39), "https://some.url",
            "Button label cannot be null and must have a maximum length of 38 characters"),
        Arguments.of(null, null,
            "Button label cannot be null and must have a maximum length of 38 characters"),
        Arguments.of(createRandomString(38), null,
            "URL of the link button cannot be null")
    );
  }

  @Test
  @DisplayName("Building an action row is successful")
  void shouldBuildActionRowSuccessfully() {
    // given: an action row with components
    var builder = MessageComponents.actionRow()
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE);

    // when: calling .build() on an action row
    var result = builder.build();

    // then: the action row is built successfully
    assertThat(result.getType()).isEqualTo(1);
    assertThat(result.getComponents()).hasSize(5);
    assertThat(result.getComponents()).contains(
        MessageComponent.builder().type(2).customId("message_component_test").label("someLabel").style(1).build(),
        MessageComponent.builder().type(2).customId("message_component_test").label("someLabel").style(1).build(),
        MessageComponent.builder().type(2).customId("message_component_test").label("someLabel").style(1).build(),
        MessageComponent.builder().type(2).customId("message_component_test").label("someLabel").style(1).build(),
        MessageComponent.builder().type(2).customId("message_component_test").label("someLabel").style(1).build()
    );
  }

  @Test
  @DisplayName("Building an action row with more than 5 buttons should throw an exception")
  void shouldThrowErrorOnTooManyButtons() {
    // given: an action row with too many buttons ( > 5 buttons)
    var builder = MessageComponents.actionRow()
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .button(MessageComponentId.MESSAGE_COMPONENT_TEST, "someLabel", ButtonStyle.BLURPLE)
        .linkButton("6", "https://some.url.com");

    // when: calling .build() on an action row
    // then: should throw an Illegal state exception with the correct error message
    Assertions.assertThrows(IllegalStateException.class, builder::build,
        "Action rows can only contain up to 5 buttons at a time");
  }

  @ParameterizedTest
  @MethodSource("illegalLinkButtonArguments")
  @DisplayName("Building an action row with link buttons throws exceptions on validation errors")
  void shouldThrowErrorOnLinkButtonValidation(String label, String url, String errorMessage) {
    //when: creating an action row with an invalid link button
    var row = MessageComponents.actionRow();

    // then: an exception is raised with the correct message
    Assertions.assertThrows(IllegalStateException.class,
        () -> row.linkButton(label, url), errorMessage);

  }
}
