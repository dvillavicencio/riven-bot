package com.deahtstroke.rivenbot.util;

import static com.deahtstroke.rivenbot.enums.MessageComponentType.ACTION_ROW;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.BUTTON;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.CHANNEL_SELECT;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.MENTIONABLE_SELECT;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.ROLE_SELECT;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.STRING_SELECT;
import static com.deahtstroke.rivenbot.enums.MessageComponentType.USER_SELECT;

import com.deahtstroke.rivenbot.dto.discord.Embedded;
import com.deahtstroke.rivenbot.dto.discord.EmbeddedField;
import com.deahtstroke.rivenbot.dto.discord.MessageComponent;
import com.deahtstroke.rivenbot.entity.ButtonStyle;
import com.deahtstroke.rivenbot.entity.RaidStatistics;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

public class MessageComponents {

  private MessageComponents() {
  }

  public static ComponentsBuilder builder() {
    return new ComponentsBuilder();
  }

  public static ActionRowBuilder actionRow() {
    return new ActionRowBuilder();
  }

  /**
   * Create a list of embeds based on n-amount of embeds
   *
   * @param embeds the list of embeds, max of 10
   * @return List of {@link Embedded}
   */
  public static List<Embedded> embeds(Embedded... embeds) {
    if (embeds.length > 10) {
      throw new IllegalStateException("Can only have 10 embeds at a time");
    }
    return Arrays.asList(embeds);
  }

  /**
   * Creates an Embedded field using a Map Entry made up of a String and Raid Statistics object
   *
   * @param entry the Map entry to use
   * @return {@link EmbeddedField}
   */
  public static EmbeddedField createField(Entry<String, RaidStatistics> entry) {
    return EmbeddedField.builder()
        .name(entry.getKey())
        .value(entry.getValue().toDiscordField())
        .inline(true)
        .build();
  }

  public static class ComponentsBuilder {

    private final List<MessageComponent> actionRows = new ArrayList<>();

    public ComponentsBuilder addActionRow(ActionRowBuilder actionRowBuilder) {
      this.actionRows.add(actionRowBuilder.build());
      return this;
    }

    public List<MessageComponent> build() {
      return this.actionRows;
    }
  }

  public static class ActionRowBuilder {

    private final List<MessageComponent> actionRowComponents = new ArrayList<>();

    public ActionRowBuilder linkButton(
        @Nullable @Length(max = 38) String label,
        @NotNull String url) {
      this.actionRowComponents.add(MessageComponent.builder()
          .url(url)
          .type(BUTTON.getType())
          .style(ButtonStyle.LINK.getButtonValue())
          .label(label).build());
      return this;
    }

    public ActionRowBuilder button(@Nullable String buttonId,
        @Nullable @Length(max = 38) String label, ButtonStyle style) {
      if (style.equals(ButtonStyle.LINK)) {
        throw new IllegalStateException("Use the method linkButton() to create a link button");
      }

      this.actionRowComponents.add(MessageComponent.builder()
          .customId(buttonId)
          .type(BUTTON.getType())
          .style(style.getButtonValue())
          .label(label)
          .build());
      return this;
    }

    public MessageComponent build() {
      boolean onlyButtons = this.actionRowComponents.stream()
          .map(MessageComponent::getType)
          .allMatch(type -> Objects.equals(type, BUTTON.getType()));
      if (onlyButtons && this.actionRowComponents.size() > 5) {
        throw new IllegalStateException("Action rows can only contain up to 5 buttons at a time");
      }

      boolean containsButtons = this.actionRowComponents.stream().map(MessageComponent::getType)
          .anyMatch(type -> Objects.equals(type, BUTTON.getType()));
      boolean containsSelectComponent = this.actionRowComponents.stream()
          .map(MessageComponent::getType)
          .allMatch(type -> Objects.equals(type, STRING_SELECT.getType()) ||
                            Objects.equals(type, USER_SELECT.getType()) ||
                            Objects.equals(type, CHANNEL_SELECT.getType()) ||
                            Objects.equals(type, MENTIONABLE_SELECT.getType()) ||
                            Objects.equals(type, ROLE_SELECT.getType()));

      if (containsButtons && containsSelectComponent) {
        throw new IllegalStateException(
            "Action rows cannot have buttons and select menus at the same time");
      }

      return MessageComponent.builder()
          .type(ACTION_ROW.getType())
          .components(this.actionRowComponents)
          .build();
    }

  }
}
