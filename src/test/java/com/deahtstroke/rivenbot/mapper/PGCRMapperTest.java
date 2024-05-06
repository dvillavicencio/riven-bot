package com.deahtstroke.rivenbot.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.deahtstroke.rivenbot.dto.destiny.PostGameCarnageReport;
import com.deahtstroke.rivenbot.entity.PGCRDetails;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

public class PGCRMapperTest {

  private PGCRMapper sut = Mappers.getMapper(PGCRMapper.class);

  @Test
  @DisplayName("Mapping a PGCR to a PGCRDetail entity is successful")
  public void mappingSuccess() {
    // given: a PGCR to map
    Long instanceId = 1L;
    PostGameCarnageReport postGameCarnageReport = new PostGameCarnageReport(Instant.now(), true,
        null);

    // when: toEntity is called
    PGCRDetails entity = sut.dtoToEntity(postGameCarnageReport, instanceId);

    // then: the entity is correctly mapped
    assertThat(entity.getFromBeginning()).isEqualTo(true);
    assertThat(entity.getInstanceId()).isEqualTo(instanceId);
  }

  @Test
  @DisplayName("Mapping a PGCR to a PGCRDetail entity is successful for activities that weren't started from the beginning")
  public void mappingForActivitiesNotFromBeginning() {
    // given: a PGCR to map
    Long instanceId = 1L;
    PostGameCarnageReport postGameCarnageReport = new PostGameCarnageReport(Instant.now(), false,
        null);

    // when: toEntity is called
    PGCRDetails entity = sut.dtoToEntity(postGameCarnageReport, instanceId);

    // then: the entity is correctly mapped
    assertThat(entity.getFromBeginning()).isEqualTo(false);
    assertThat(entity.getInstanceId()).isEqualTo(instanceId);
  }

}
