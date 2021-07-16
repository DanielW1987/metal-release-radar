package rocks.metaldetector.service.summary;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.support.DetectorSort;
import rocks.metaldetector.support.Page;
import rocks.metaldetector.support.PageRequest;
import rocks.metaldetector.support.TimeRange;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static rocks.metaldetector.service.summary.SummaryServiceImpl.RESULT_LIMIT;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.TIME_RANGE_MONTHS;
import static rocks.metaldetector.support.DetectorSort.Direction.ASC;
import static rocks.metaldetector.support.DetectorSort.Direction.DESC;

@Component
@AllArgsConstructor
public class ReleaseCollector {

  private final ReleaseService releaseService;

  public List<ReleaseDto> collectUpcomingReleases(List<ArtistDto> artists) {
    LocalDate tomorrow = LocalDate.now().plusDays(1);
    TimeRange timeRange = new TimeRange(tomorrow, tomorrow.plusMonths(TIME_RANGE_MONTHS));
    DetectorSort sort = new DetectorSort("releaseDate", ASC);
    return collectPaginatedReleases(artists, timeRange, sort);
  }

  public List<ReleaseDto> collectRecentReleases(List<ArtistDto> artists) {
    LocalDate now = LocalDate.now();
    TimeRange timeRange = new TimeRange(now.minusMonths(TIME_RANGE_MONTHS), now);
    DetectorSort sort = new DetectorSort("releaseDate", DESC);
    return collectPaginatedReleases(artists, timeRange, sort);
  }

  public List<ReleaseDto> collectReleases(List<ArtistDto> artists, TimeRange timeRange) {
    if (artists.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> artistNames = artists.stream().map(ArtistDto::getArtistName).collect(Collectors.toList());
    return releaseService.findAllReleases(artistNames, timeRange);
  }

  private List<ReleaseDto> collectPaginatedReleases(List<ArtistDto> artists, TimeRange timeRange, DetectorSort sort) {
    if (artists.isEmpty()) {
      return Collections.emptyList();
    }

    List<String> artistNames = artists.stream().map(ArtistDto::getArtistName).collect(Collectors.toList());
    PageRequest pageRequest = new PageRequest(1, RESULT_LIMIT, sort);
    Page<ReleaseDto> releasePage = releaseService.findReleases(artistNames, timeRange, null, pageRequest);
    return releasePage.getItems();
  }
}
