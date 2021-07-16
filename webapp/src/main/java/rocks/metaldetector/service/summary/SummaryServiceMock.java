package rocks.metaldetector.service.summary;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.web.api.response.SummaryResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static rocks.metaldetector.support.ImageSize.M;
import static rocks.metaldetector.support.ImageSize.S;

@Service
@Profile("mockmode")
public class SummaryServiceMock implements SummaryService {

  @Override
  public SummaryResponse createSummaryResponse() {
    LocalDateTime now = LocalDateTime.now();
    List<ReleaseDto> upcomingReleases = createUpcomingReleases(now.toLocalDate());
    List<ReleaseDto> recentReleases = createRecentReleases(now.toLocalDate());
    List<ArtistDto> recentlyFollowedArtists = createRecentlyFollowedArtists(now);

    return SummaryResponse.builder()
        .upcomingReleases(upcomingReleases)
        .recentReleases(recentReleases)
        .mostExpectedReleases(upcomingReleases)
        .recentlyFollowedArtists(recentlyFollowedArtists)
        .favoriteCommunityArtists(recentlyFollowedArtists)
        .build();
  }

  @Override
  public List<ReleaseDto> findTopReleases(TimeRange timeRange, int minFollower, int maxReleases) {
    return List.of(ReleaseDto.builder().albumTitle("Sons of Northern Darkness").artist("Immortal").releaseDate(LocalDate.now()).build());
  }

  private List<ReleaseDto> createUpcomingReleases(LocalDate now) {
    ReleaseDto waldvolk = ReleaseDto.builder().albumTitle("Waldvolk").artist("XIV Dark Centuries").releaseDate(now.plusDays(11)).coverUrl("/images/dummy/xiv-dark-centuries_waldvolk.jpg").build();
    ReleaseDto transilvanianHunger = ReleaseDto.builder().albumTitle("Transilvanian Hunger").artist("Darkthrone").releaseDate(now.plusDays(21)).coverUrl("/images/dummy/darkthrone_transilvanian-hunger.jpg").build();
    ReleaseDto traktat = ReleaseDto.builder().albumTitle("Traktat").artist("Karg").releaseDate(now.plusDays(25)).coverUrl("/images/dummy/karg_traktat.jpg").build();
    return List.of(waldvolk, transilvanianHunger, traktat);
  }

  private List<ReleaseDto> createRecentReleases(LocalDate now) {
    ReleaseDto freierWilleFreierGeist = ReleaseDto.builder().albumTitle("Freier Wille, Freier Geist").artist("Thormesis").releaseDate(now.minusDays(2)).coverUrl("/images/dummy/thormesis_freier-wille-freier-geist.jpg").build();
    ReleaseDto sonsOfNorthernDarkness = ReleaseDto.builder().albumTitle("Sons of Northern Darkness").artist("Immortal").releaseDate(now.minusDays(13)).coverUrl("/images/dummy/immortal_sons-of_northern_darkness.jpg").build();
    ReleaseDto ritualeSatanum = ReleaseDto.builder().albumTitle("Rituale Satanum").artist("Behexen").releaseDate(now.minusDays(25)).coverUrl("/images/dummy/behexen_rituale-satanum.jpg").build();
    ReleaseDto ageOfExcuse = ReleaseDto.builder().albumTitle("Age of Excuse").artist("Mgla").releaseDate(now.minusDays(29)).coverUrl("/images/dummy/mgla_age-of-excuse.jpg").build();
    return List.of(ageOfExcuse, ritualeSatanum, sonsOfNorthernDarkness, freierWilleFreierGeist);
  }

  private List<ArtistDto> createRecentlyFollowedArtists(LocalDateTime now) {
    ArtistDto harakiriForTheSky = ArtistDto.builder().artistName("Harakiri For the Sky").images(Map.of(S,"/images/dummy/harakiri-for-the-sky.jpg")).followedSince(now.minusDays(3)).follower(666).build();
    ArtistDto marduk = ArtistDto.builder().artistName("Marduk").images(Map.of(M, "/images/dummy/marduk.jpg")).followedSince(now.minusDays(7)).follower(666).build();
    ArtistDto abbath = ArtistDto.builder().artistName("Abbath").images(Map.of(M, "/images/dummy/abbath.jpg")).followedSince(now.minusDays(15)).follower(666).build();
    ArtistDto alcest = ArtistDto.builder().artistName("Alcest").images(Map.of(M, "/images/dummy/alcest.jpg")).followedSince(now.minusDays(26)).follower(666).build();
    return List.of(alcest, abbath, marduk, harakiriForTheSky);
  }
}
