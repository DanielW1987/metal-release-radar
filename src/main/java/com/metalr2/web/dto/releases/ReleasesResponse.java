package com.metalr2.web.dto.releases;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({
    "totalReleases",
    "releases"
})
@NoArgsConstructor
public class ReleasesResponse {

  @JsonProperty("releases")
  private Iterable<ReleaseDto> releases;

}
