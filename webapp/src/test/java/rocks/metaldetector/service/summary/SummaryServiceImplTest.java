package rocks.metaldetector.service.summary;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.testutil.DtoFactory.ArtistDtoFactory;
import rocks.metaldetector.testutil.DtoFactory.ReleaseDtoFactory;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.MIN_FOLLOWER;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.RESULT_LIMIT;

@ExtendWith(MockitoExtension.class)
class SummaryServiceImplTest implements WithAssertions {

  @Mock
  private ReleaseCollector releaseCollector;

  @Mock
  private ArtistCollector artistCollector;

  @Mock
  private FollowArtistService followArtistService;

  @InjectMocks
  private SummaryServiceImpl underTest;

  @AfterEach
  void tearDown() {
    reset(releaseCollector, artistCollector, followArtistService);
  }

  @Nested
  @DisplayName("Tests for getting summary")
  class SummaryTest {

    @Test
    @DisplayName("followArtistService is called to get current user's followed artists")
    void test_follow_artist_service_called() {
      // when
      underTest.createSummaryResponse();

      // then
      verify(followArtistService).getFollowedArtistsOfCurrentUser();
    }

    @Test
    @DisplayName("releaseCollector is called with followed artists to get upcoming releases")
    void test_release_collector_upcoming_releases() {
      // given
      var artists = List.of(ArtistDtoFactory.createDefault());
      doReturn(artists).when(followArtistService).getFollowedArtistsOfCurrentUser();

      // when
      underTest.createSummaryResponse();

      // then
      verify(releaseCollector).collectUpcomingReleases(eq(artists));
    }

    @Test
    @DisplayName("releaseCollector is called with followed artists to get recent releases")
    void test_release_collector_recent_releases() {
      // given
      var artists = List.of(ArtistDtoFactory.createDefault());
      doReturn(artists).when(followArtistService).getFollowedArtistsOfCurrentUser();

      // when
      underTest.createSummaryResponse();

      // then
      verify(releaseCollector).collectRecentReleases(eq(artists));
    }

    @Test
    @DisplayName("releaseCollector is called with top artists to get most expected releases")
    void test_release_collector_most_expected_releases() {
      // given
      var artists = List.of(ArtistDtoFactory.createDefault());
      doReturn(artists).when(artistCollector).collectTopFollowedArtists(anyInt());

      // when
      underTest.createSummaryResponse();

      // then
      verify(releaseCollector).collectUpcomingReleases(eq(artists));
    }

    @Test
    @DisplayName("artistCollector is called to get top followed artists")
    void test_artist_collector_top_artists() {
      // when
      underTest.createSummaryResponse();

      // then
      verify(artistCollector).collectTopFollowedArtists(MIN_FOLLOWER);
    }

    @Test
    @DisplayName("artistCollector is called to get recently followed artists")
    void test_artist_collector_recently_artists() {
      // when
      underTest.createSummaryResponse();

      // then
      verify(artistCollector).collectRecentlyFollowedArtists(RESULT_LIMIT);
    }

    @Test
    @DisplayName("upcoming releases are returned")
    void test_upcoming_releases_returned() {
      // given
      var releases = List.of(ReleaseDtoFactory.createDefault());
      doReturn(releases).when(releaseCollector).collectUpcomingReleases(anyList());

      // when
      var result = underTest.createSummaryResponse();

      // then
      assertThat(result.getUpcomingReleases()).isEqualTo(releases);
    }

    @Test
    @DisplayName("recent releases are returned")
    void test_recent_releases_returned() {
      // given
      var releases = List.of(ReleaseDtoFactory.createDefault());
      doReturn(releases).when(releaseCollector).collectRecentReleases(anyList());

      // when
      var result = underTest.createSummaryResponse();

      // then
      assertThat(result.getRecentReleases()).isEqualTo(releases);
    }

    @Test
    @DisplayName("top followed artists are filtered and returned")
    void test_top_followed_artists_returned() {
      // given
      var artist = ArtistDtoFactory.createDefault();
      var artists = List.of(artist, artist, artist, artist, artist);
      doReturn(artists).when(artistCollector).collectTopFollowedArtists(anyInt());

      // when
      var result = underTest.createSummaryResponse();

      // then
      assertThat(result.getFavoriteCommunityArtists()).isEqualTo(artists.subList(0, RESULT_LIMIT));
    }

    @Test
    @DisplayName("most expected releases are returned")
    void test_most_expected_releases_returned() {
      // given
      var releases = List.of(ReleaseDtoFactory.createDefault());
      doReturn(Collections.emptyList()).when(releaseCollector).collectUpcomingReleases(anyList());
      doReturn(releases).when(releaseCollector).collectUpcomingReleases(anyList());

      // when
      var result = underTest.createSummaryResponse();

      // then
      assertThat(result.getMostExpectedReleases()).isEqualTo(releases);
    }

    @Test
    @DisplayName("recently followed artists are returned")
    void test_recently_followed_artists_returned() {
      // given
      var artists = List.of(ArtistDtoFactory.createDefault());
      doReturn(artists).when(artistCollector).collectRecentlyFollowedArtists(anyInt());

      // when
      var result = underTest.createSummaryResponse();

      // then
      assertThat(result.getRecentlyFollowedArtists()).isEqualTo(artists);
    }
  }

  @Nested
  @DisplayName("Tests for top releases")
  class TopReleasesTest {

    @Test
    @DisplayName("artistCollector is called")
    void test_artist_collector_called() {
      // given
      var minFollower = 1;

      // when
      underTest.findTopReleases(new TimeRange(), minFollower, 10);

      // then
      verify(artistCollector).collectTopFollowedArtists(minFollower);
    }

    @Test
    @DisplayName("releaseCollector is called")
    void test_release_collector_called() {
      // given
      var timeRange = new TimeRange(LocalDate.now(), null);
      var artists = List.of(ArtistDtoFactory.createDefault());
      doReturn(artists).when(artistCollector).collectTopFollowedArtists(anyInt());

      // when
      underTest.findTopReleases(timeRange, 1, 10);

      // then
      verify(releaseCollector).collectReleases(artists, timeRange);
    }

    @Test
    @DisplayName("returned releases are sorted by followers")
    void test_releases_sorted() {
      // given
      var artist1 = ArtistDtoFactory.withName("a");
      var artist2 = ArtistDtoFactory.withName("b");
      artist1.setFollower(2);
      artist2.setFollower(1);
      var release1 = ReleaseDtoFactory.withArtistName(artist1.getArtistName());
      var release2 = ReleaseDtoFactory.withArtistName(artist2.getArtistName());
      doReturn(List.of(artist1, artist2)).when(artistCollector).collectTopFollowedArtists(anyInt());
      doReturn(List.of(release2, release1)).when(releaseCollector).collectReleases(any(), any());

      // when
      var result = underTest.findTopReleases(new TimeRange(), 1, 10);

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0)).isEqualTo(release1);
      assertThat(result.get(1)).isEqualTo(release2);
    }

    @Test
    @DisplayName("releases are limited to given value")
    void test_releases_limited() {
      // given
      var maxReleases = 1;
      var artist1 = ArtistDtoFactory.withName("a");
      var artist2 = ArtistDtoFactory.withName("b");
      artist1.setFollower(2);
      artist2.setFollower(1);
      var release1 = ReleaseDtoFactory.withArtistName(artist1.getArtistName());
      var release2 = ReleaseDtoFactory.withArtistName(artist2.getArtistName());
      doReturn(List.of(artist1, artist2)).when(artistCollector).collectTopFollowedArtists(anyInt());
      doReturn(List.of(release2, release1)).when(releaseCollector).collectReleases(any(), any());

      // when
      var result = underTest.findTopReleases(new TimeRange(), 1, maxReleases);

      // then
      assertThat(result).hasSize(maxReleases);
      assertThat(result).containsExactly(release1);
    }
  }
}
