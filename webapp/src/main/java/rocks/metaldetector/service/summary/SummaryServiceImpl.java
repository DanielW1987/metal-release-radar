package rocks.metaldetector.service.summary;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.web.api.response.SummaryResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Profile({"default", "preview", "prod"})
@AllArgsConstructor
public class SummaryServiceImpl implements SummaryService {

  public static final int MIN_FOLLOWER = 2;
  public static final int RESULT_LIMIT = 4;
  public static final int TIME_RANGE_MONTHS = 6;

  private final ReleaseCollector releaseCollector;
  private final ArtistCollector artistCollector;
  private final FollowArtistService followArtistService;

  @Override
  public SummaryResponse createSummaryResponse() {
    List<ArtistDto> currentUsersFollowedArtists = followArtistService.getFollowedArtistsOfCurrentUser();
    List<ArtistDto> mostFollowedArtists = artistCollector.collectTopFollowedArtists(MIN_FOLLOWER).stream().limit(RESULT_LIMIT).collect(Collectors.toList());
    List<ArtistDto> recentlyFollowedArtists = artistCollector.collectRecentlyFollowedArtists(RESULT_LIMIT);

    List<ReleaseDto> upcomingReleases = releaseCollector.collectUpcomingReleases(currentUsersFollowedArtists);
    List<ReleaseDto> recentReleases = releaseCollector.collectRecentReleases(currentUsersFollowedArtists);
    List<ReleaseDto> mostExpectedReleases = releaseCollector.collectUpcomingReleases(mostFollowedArtists);

    return SummaryResponse.builder()
        .upcomingReleases(upcomingReleases)
        .recentReleases(recentReleases)
        .favoriteCommunityArtists(mostFollowedArtists)
        .mostExpectedReleases(mostExpectedReleases)
        .recentlyFollowedArtists(recentlyFollowedArtists)
        .build();
  }

  @Override
  public List<ReleaseDto> findTopReleases(TimeRange timeRange, int minFollower, int maxReleases) {
    List<ArtistDto> artists = artistCollector.collectTopFollowedArtists(minFollower);
    Map<String, Integer> followersPerArtist = artists.stream()
        .collect(Collectors.groupingBy(artistDto -> artistDto.getArtistName().toLowerCase(),
                                       Collectors.summingInt(ArtistDto::getFollower)));
    return releaseCollector.collectReleases(artists, timeRange).stream()
        .sorted(Comparator.comparingInt(
            (ReleaseDto release) -> followersPerArtist.get(release.getArtist().toLowerCase()))
                    .reversed())
        .limit(maxReleases)
        .collect(Collectors.toList());
  }
}
