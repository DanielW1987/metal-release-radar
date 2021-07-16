package rocks.metaldetector.service.summary;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.support.DetectorSort;
import rocks.metaldetector.support.Page;
import rocks.metaldetector.support.PageRequest;
import rocks.metaldetector.support.Pagination;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.testutil.DtoFactory.ArtistDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.ReleaseDtoFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.RESULT_LIMIT;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.TIME_RANGE_MONTHS;
import static rocks.metaldetector.support.DetectorSort.Direction.ASC;
import static rocks.metaldetector.support.DetectorSort.Direction.DESC;

@ExtendWith(MockitoExtension.class)
class ReleaseCollectorTest implements WithAssertions {

  @Mock
  private ReleaseService releaseService;

  @InjectMocks
  private ReleaseCollector underTest;

  @Test
  @DisplayName("collecting upcoming releases does not call releaseService when no artists are given")
  void test_upcoming_releases_does_not_call_release_service() {
    // when
    underTest.collectUpcomingReleases(Collections.emptyList());

    // then
    verifyNoInteractions(releaseService);
  }

  @Test
  @DisplayName("collecting upcoming releases calls releaseService with followed artists' names")
  void test_upcoming_releases_calls_release_service_with_artist_names() {
    // given
    var expectedArtistNames = List.of("A");
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectUpcomingReleases(artists);

    // then
    verify(releaseService).findReleases(eq(expectedArtistNames), any(), any(), any());
  }

  @Test
  @DisplayName("collecting upcoming releases calls releaseService with correct time range")
  void test_upcoming_releases_calls_release_service_with_time_range() {
    // given
    var tomorrow = LocalDate.now().plusDays(1);
    var expectedTimeRange = new TimeRange(tomorrow, tomorrow.plusMonths(TIME_RANGE_MONTHS));
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectUpcomingReleases(artists);

    // then
    verify(releaseService).findReleases(any(), eq(expectedTimeRange), any(), any());
  }

  @Test
  @DisplayName("collecting upcoming releases calls releaseService without query")
  void test_upcoming_releases_calls_release_service_without_query() {
    // given
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectUpcomingReleases(artists);

    // then
    verify(releaseService).findReleases(any(), any(), eq(null), any());
  }

  @Test
  @DisplayName("collecting upcoming releases calls releaseService with correct page request and sorting")
  void test_upcoming_releases_calls_release_service_with_page_request() {
    // given
    var sorting = new DetectorSort("releaseDate", ASC);
    var expectedPageRequest = new PageRequest(1, RESULT_LIMIT, sorting);
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectUpcomingReleases(artists);

    // then
    verify(releaseService).findReleases(any(), any(), any(), eq(expectedPageRequest));
  }

  @Test
  @DisplayName("collecting upcoming releases returns list of releases")
  void test_upcoming_returns_releases() {
    // given
    var artists = List.of(ArtistDtoFactory.withName("A"));
    var releases = List.of(ReleaseDtoFactory.createDefault(), ReleaseDtoFactory.createDefault());
    doReturn(new Page<>(releases, new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    var result = underTest.collectUpcomingReleases(artists);

    // then
    assertThat(result).isEqualTo(releases);
  }

  @Test
  @DisplayName("collecting recent releases does not call releaseService when no artists are given")
  void test_recent_releases_does_not_call_release_service() {
    // when
    underTest.collectRecentReleases(Collections.emptyList());

    // then
    verifyNoInteractions(releaseService);
  }

  @Test
  @DisplayName("collecting recent releases calls releaseService with followed artists' names")
  void test_recent_releases_calls_release_service_with_artist_names() {
    // given
    var expectedArtistNames = List.of("A");
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectRecentReleases(artists);

    // then
    verify(releaseService).findReleases(eq(expectedArtistNames), any(), any(), any());
  }

  @Test
  @DisplayName("collecting recent releases calls releaseService with correct time range")
  void test_recent_releases_calls_release_service_with_time_range() {
    // given
    var now = LocalDate.now();
    var expectedTimeRange = new TimeRange(now.minusMonths(TIME_RANGE_MONTHS), now);
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectRecentReleases(artists);

    // then
    verify(releaseService).findReleases(any(), eq(expectedTimeRange), any(), any());
  }

  @Test
  @DisplayName("collecting recent releases calls releaseService without query")
  void test_recent_releases_calls_release_service_without_query() {
    // given
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectRecentReleases(artists);

    // then
    verify(releaseService).findReleases(any(), any(), eq(null), any());
  }

  @Test
  @DisplayName("collecting recent releases calls releaseService with correct page request and sorting")
  void test_recent_releases_calls_release_service_with_page_request() {
    // given
    var expectedSorting = new DetectorSort("releaseDate", DESC);
    var expectedPageRequest = new PageRequest(1, RESULT_LIMIT, expectedSorting);
    var artists = List.of(ArtistDtoFactory.withName("A"));
    doReturn(new Page<>(Collections.emptyList(), new Pagination())).when(releaseService).findReleases(any(), any(), any(), any());

    // when
    underTest.collectRecentReleases(artists);

    // then
    verify(releaseService).findReleases(any(), any(), any(), eq(expectedPageRequest));
  }

  @Test
  @DisplayName("collectReleases: returns empty list if artists are empty")
  void test_collect_releases_returns_empty_list() {
    // when
    var result = underTest.collectReleases(Collections.emptyList(), new TimeRange());

    // then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("collectReleases: releaseService is not called if artists are empty")
  void test_collect_releases_not_calling_release_service() {
    // when
    underTest.collectReleases(Collections.emptyList(), new TimeRange());

    // then
    verifyNoInteractions(releaseService);
  }

  @Test
  @DisplayName("collectReleases: releaseService is called with artist names and time range")
  void test_collect_releases_called_correctly() {
    // given
    var timeRange = new TimeRange(LocalDate.now(), LocalDate.now());
    var artist = ArtistDtoFactory.withName("a");
    var expectedArtistNames = List.of("a");

    // when
    underTest.collectReleases(List.of(artist), timeRange);

    // then
    verify(releaseService).findAllReleases(expectedArtistNames, timeRange);
  }

  @Test
  @DisplayName("collectReleases: returns releaseDtos")
  void test_collect_releases_returns_dtos() {
    // given
    var releases = List.of(ReleaseDtoFactory.createDefault());
    var artist = ArtistDtoFactory.createDefault();
    doReturn(releases).when(releaseService).findAllReleases(any(), any());

    // when
    var result = underTest.collectReleases(List.of(artist), new TimeRange());

    // then
    assertThat(result).containsAll(releases);
  }
}
