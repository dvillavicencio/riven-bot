package com.deahtstroke.rivenbot.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.dto.destiny.PostGameCarnageReport;
import com.deahtstroke.rivenbot.entity.PGCRDetails;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class PGCRMapperTest {

  private final PGCRMapper sut = Mappers.getMapper(PGCRMapper.class);

  @Test
  @DisplayName("Mapping a PGCR to a PGCRDetail entity is successful")
  void mappingSuccess() {
    // given: a Post Game Carnage Report to map
    Long instanceId = 1L;
    PostGameCarnageReport postGameCarnageReport = new PostGameCarnageReport(Instant.now(), true,
        null);

    // when: toEntity is called
    PGCRDetails entity = sut.dtoToEntity(postGameCarnageReport, instanceId);

    // then: the entity is correctly mapped
    assertThat(entity.getFromBeginning()).isTrue();
    assertThat(entity.getInstanceId()).isEqualTo(String.valueOf(instanceId));
  }

  @Test
  @DisplayName("Mapping a PGCR to a PGCRDetail entity is successful for activities that weren't started from the beginning")
  void mappingForActivitiesNotFromBeginning() {
    // given: a Post Game Carnage Report to map
    Long instanceId = 1L;
    PostGameCarnageReport postGameCarnageReport = new PostGameCarnageReport(Instant.now(), false,
        null);

    // when: toEntity is called
    PGCRDetails entity = sut.dtoToEntity(postGameCarnageReport, instanceId);

    // then: the entity is correctly mapped
    assertThat(entity.getFromBeginning()).isFalse();
    assertThat(entity.getInstanceId()).isEqualTo(String.valueOf(instanceId));
  }

}
