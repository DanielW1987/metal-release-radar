package rocks.metaldetector.web.api.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class TopReleasesRequest extends ReleasesRequest {

  @Min(1L)
  private int maxReleases;

  @Min(1L)
  private int minFollowers;
}
