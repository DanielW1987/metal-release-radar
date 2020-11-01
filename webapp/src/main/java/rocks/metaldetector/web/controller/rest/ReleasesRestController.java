package rocks.metaldetector.web.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.butler.facade.dto.ImportJobResultDto;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.support.Endpoints;
import rocks.metaldetector.support.Page;
import rocks.metaldetector.support.PageRequest;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.web.api.request.PaginatedReleasesRequest;
import rocks.metaldetector.web.api.request.ReleasesRequest;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static org.springframework.http.HttpStatus.CREATED;

@RestController
@AllArgsConstructor
public class ReleasesRestController {

  private final ReleaseService releaseService;
  private final FollowArtistService followArtistService;

  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  @GetMapping(path = Endpoints.Rest.QUERY_ALL_RELEASES,
              produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<ReleaseDto>> findAllReleases(@Valid ReleasesRequest request) {
    var timeRange = new TimeRange(request.getDateFrom(), request.getDateTo());
    List<ReleaseDto> releaseDtos = releaseService.findAllReleases(emptyList(), timeRange);
    return ResponseEntity.ok(releaseDtos);
  }

  @GetMapping(path = Endpoints.Rest.QUERY_RELEASES,
              produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Page<ReleaseDto>> findReleases(@Valid PaginatedReleasesRequest request) {
    var timeRange = new TimeRange(request.getDateFrom(), request.getDateTo());
    var pageRequest = new PageRequest(request.getPage(), request.getSize());
    Page<ReleaseDto> releasePage = releaseService.findReleases(emptyList(), timeRange, pageRequest);
    return ResponseEntity.ok(releasePage);
  }

  @GetMapping(path = Endpoints.Rest.QUERY_MY_RELEASES,
          produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Page<ReleaseDto>> findReleasesOfFollowedArtists(@Valid PaginatedReleasesRequest request) {
    var timeRange = new TimeRange(request.getDateFrom(), request.getDateTo());
    var pageRequest = new PageRequest(request.getPage(), request.getSize());
    var followedArtists = followArtistService.getFollowedArtistsOfCurrentUser().stream().map(ArtistDto::getArtistName).collect(Collectors.toList());
    Page<ReleaseDto> releasePage = releaseService.findReleases(followedArtists, timeRange, pageRequest);
    return ResponseEntity.ok(releasePage);
  }

  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  @PostMapping(path = Endpoints.Rest.IMPORT_JOB)
  public ResponseEntity<Void> createImportJob() {
    releaseService.createImportJob();
    return ResponseEntity.status(CREATED).build();
  }

  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  @GetMapping(path = Endpoints.Rest.IMPORT_JOB,
              produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<ImportJobResultDto>> getImportJobResults() {
    List<ImportJobResultDto> response = releaseService.queryImportJobResults();
    return ResponseEntity.ok(response);
  }

  @PreAuthorize("hasRole('ROLE_ADMINISTRATOR')")
  @PostMapping(path = Endpoints.Rest.COVER_JOB)
  public ResponseEntity<Void> createRetryCoverDownloadJob() {
    releaseService.createRetryCoverDownloadJob();
    return ResponseEntity.ok().build();
  }
}
