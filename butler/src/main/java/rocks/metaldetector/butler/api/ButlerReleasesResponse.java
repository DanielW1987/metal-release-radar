package rocks.metaldetector.butler.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ButlerReleasesResponse {

  @JsonProperty("releases")
  private List<ButlerRelease> releases;

}
