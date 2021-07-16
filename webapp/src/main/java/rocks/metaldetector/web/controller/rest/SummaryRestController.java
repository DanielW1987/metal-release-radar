package rocks.metaldetector.web.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.service.summary.SummaryService;
import rocks.metaldetector.support.Endpoints;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.web.api.request.TopReleasesRequest;
import rocks.metaldetector.web.api.response.SummaryResponse;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
public class SummaryRestController {

  private final SummaryService summaryService;

  @GetMapping(path = Endpoints.Rest.HOME, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<SummaryResponse> handleSummaryRequest() {
    SummaryResponse summaryResponse = summaryService.createSummaryResponse();
    return ResponseEntity.ok(summaryResponse);
  }

  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  @GetMapping(path = Endpoints.Rest.TOP_RELEASES, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<List<ReleaseDto>> findTopReleases(@Valid TopReleasesRequest request) {
    var timeRange = new TimeRange(request.getDateFrom(), request.getDateTo());
    var releaseDtos = summaryService.findTopReleases(timeRange, request.getMinFollowers(), request.getMaxReleases());
    return ResponseEntity.ok(releaseDtos);
  }
}
