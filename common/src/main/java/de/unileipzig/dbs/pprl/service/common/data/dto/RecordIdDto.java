package de.unileipzig.dbs.pprl.service.common.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.unileipzig.dbs.pprl.core.common.model.impl.RecordIdComposed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"unique", "source", "local", "global", "blocks"})
public class RecordIdDto {

  private String local;

  private String source;

  private String global;

  private String unique;

  @Singular
  private List<String> blocks = new ArrayList<>();

  public RecordIdDto duplicate() {
    RecordIdDtoBuilder builder = RecordIdDto.builder()
      .local(local)
      .source(source)
      .unique(unique)
      .global(this.global);
    if (blocks != null) {
      builder.blocks(new ArrayList<>(blocks));
    }
    return builder.build();
  }

  @JsonIgnore
  public String getUniqueLike() {
    return RecordIdComposed.toComposedId(this.local, this.source);
  }
}
