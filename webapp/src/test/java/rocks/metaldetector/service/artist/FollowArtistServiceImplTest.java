package rocks.metaldetector.service.artist;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.discogs.facade.DiscogsService;
import rocks.metaldetector.persistence.domain.artist.ArtistEntity;
import rocks.metaldetector.persistence.domain.artist.ArtistRepository;
import rocks.metaldetector.persistence.domain.artist.ArtistSource;
import rocks.metaldetector.persistence.domain.artist.FollowActionEntity;
import rocks.metaldetector.persistence.domain.artist.FollowActionRepository;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.service.artist.transformer.ArtistDtoTransformer;
import rocks.metaldetector.service.artist.transformer.ArtistEntityTransformer;
import rocks.metaldetector.spotify.facade.SpotifyService;
import rocks.metaldetector.testutil.DtoFactory.ArtistDtoFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static rocks.metaldetector.persistence.domain.artist.ArtistSource.DISCOGS;
import static rocks.metaldetector.persistence.domain.artist.ArtistSource.SPOTIFY;
import static rocks.metaldetector.testutil.DtoFactory.DiscogsArtistDtoFactory;
import static rocks.metaldetector.testutil.DtoFactory.SpotifyArtistDtoFactory;

@ExtendWith(MockitoExtension.class)
class FollowArtistServiceImplTest implements WithAssertions {

  private static final String EXTERNAL_ID = "252211";
  private static final ArtistSource ARTIST_SOURCE = DISCOGS;

  @Mock
  private ArtistDtoTransformer artistDtoTransformer;

  @Mock
  private ArtistEntityTransformer artistEntityTransformer;

  @Mock
  private ArtistRepository artistRepository;

  @Mock
  private ArtistService artistService;

  @Mock
  private CurrentUserSupplier currentUserSupplier;

  @Mock
  private DiscogsService discogsService;

  @Mock
  private FollowActionRepository followActionRepository;

  @Mock
  private SpotifyService spotifyService;

  @InjectMocks
  private FollowArtistServiceImpl underTest;

  @Mock
  private UserEntity userEntity;

  @AfterEach
  void tearDown() {
    reset(artistDtoTransformer, artistEntityTransformer, artistRepository, artistService, currentUserSupplier,
          discogsService, followActionRepository, spotifyService, userEntity);
  }

  @Test
  @DisplayName("Artist is fetched from repository if it already exists on follow")
  void follow_should_get_artist() {
    // given
    when(artistRepository.existsByExternalIdAndSource(anyString(), any())).thenReturn(true);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(ArtistEntityFactory.withExternalId(EXTERNAL_ID)));
    when(currentUserSupplier.get()).thenReturn(userEntity);

    // when
    underTest.follow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    InOrder inOrderVerifier = inOrder(artistRepository);
    inOrderVerifier.verify(artistRepository).existsByExternalIdAndSource(EXTERNAL_ID, ARTIST_SOURCE);
    inOrderVerifier.verify(artistRepository).findByExternalIdAndSource(EXTERNAL_ID, ARTIST_SOURCE);
  }

  @Test
  @DisplayName("Current user is fetched on follow")
  void follow_should_get_current_user() {
    // given
    when(artistRepository.existsByExternalIdAndSource(anyString(), any())).thenReturn(true);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(ArtistEntityFactory.withExternalId(EXTERNAL_ID)));
    when(currentUserSupplier.get()).thenReturn(userEntity);

    // when
    underTest.follow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(currentUserSupplier).get();
  }

  @Test
  @DisplayName("Spotify Artist is only searched on Spotify if it does not yet exist on follow")
  void follow_should_search_spotify() {
    // given
    when(spotifyService.searchArtistById(anyString())).thenReturn(SpotifyArtistDtoFactory.createDefault());
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(artistRepository.save(any())).thenReturn(ArtistEntityFactory.withExternalId(EXTERNAL_ID));

    // when
    underTest.follow(EXTERNAL_ID, SPOTIFY);

    // then
    verify(spotifyService).searchArtistById(EXTERNAL_ID);
    verifyNoInteractions(discogsService);
  }

  @Test
  @DisplayName("Discogs Artist is only searched on Discogs if it does not yet exist on follow")
  void follow_should_search_discogs() {
    // given
    when(discogsService.searchArtistById(anyString())).thenReturn(DiscogsArtistDtoFactory.createDefault());
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(artistRepository.save(any())).thenReturn(ArtistEntityFactory.withExternalId(EXTERNAL_ID));

    // when
    underTest.follow(EXTERNAL_ID, DISCOGS);

    // then
    verify(discogsService).searchArtistById(EXTERNAL_ID);
    verifyNoInteractions(spotifyService);
  }

  @Test
  @DisplayName("Neither Spotify nor Discogs is searched if artist already exists")
  void follow_should_not_search() {
    // given
    when(artistRepository.existsByExternalIdAndSource(anyString(), any())).thenReturn(true);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(ArtistEntityFactory.withExternalId(EXTERNAL_ID)));
    when(currentUserSupplier.get()).thenReturn(userEntity);

    // when
    underTest.follow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verifyNoInteractions(spotifyService);
    verifyNoInteractions(discogsService);
  }

  @Test
  @DisplayName("Should transform SpotifyArtistDto to ArtistEntity")
  void should_transform_spotify_artist_to_artist_entity() {
    // given
    var spotifyArtist = SpotifyArtistDtoFactory.createDefault();
    when(spotifyService.searchArtistById(anyString())).thenReturn(spotifyArtist);
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(artistRepository.save(any())).thenReturn(ArtistEntityFactory.withExternalId(EXTERNAL_ID));

    // when
    underTest.follow(EXTERNAL_ID, SPOTIFY);

    // then
    verify(artistEntityTransformer).transformSpotifyArtistDto(spotifyArtist);
  }

  @Test
  @DisplayName("Should transform DiscogsArtistDto to ArtistEntity")
  void should_transform_discogs_artist_to_artist_entity() {
    // given
    var discogsArtist = DiscogsArtistDtoFactory.createDefault();
    when(discogsService.searchArtistById(anyString())).thenReturn(discogsArtist);
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(artistRepository.save(any())).thenReturn(ArtistEntityFactory.withExternalId(EXTERNAL_ID));

    // when
    underTest.follow(EXTERNAL_ID, DISCOGS);

    // then
    verify(artistEntityTransformer).transformDiscogsArtistDto(discogsArtist);
  }

  @Test
  @DisplayName("Artist is saved in repository if it does not yet exist on follow")
  void follow_should_save_artist() {
    // given
    var artistEntity = ArtistEntityFactory.withExternalId(EXTERNAL_ID);
    when(discogsService.searchArtistById(anyString())).thenReturn(DiscogsArtistDtoFactory.createDefault());
    when(artistEntityTransformer.transformDiscogsArtistDto(any())).thenReturn(artistEntity);
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(artistRepository.save(any())).thenReturn(artistEntity);

    // when
    underTest.follow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(artistRepository).save(artistEntity);
  }

  @Test
  @DisplayName("FollowAction is saved on follow")
  void follow_should_add_artist_to_user() {
    // given
    ArtistEntity artist = ArtistEntityFactory.withExternalId(EXTERNAL_ID);
    when(artistRepository.existsByExternalIdAndSource(anyString(), any())).thenReturn(true);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(artist));
    when(currentUserSupplier.get()).thenReturn(userEntity);

    // when
    underTest.follow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    FollowActionEntity followAction = FollowActionEntity.builder().user(userEntity).artist(artist).build();
    verify(followActionRepository).save(followAction);
  }

  @Test
  @DisplayName("Artist is fetched from repository on unfollow")
  void unfollow_should_fetch_artist_from_repository() {
    // given
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(ArtistEntityFactory.withExternalId(EXTERNAL_ID)));

    // when
    underTest.unfollow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(artistRepository).findByExternalIdAndSource(EXTERNAL_ID, ARTIST_SOURCE);
  }

  @Test
  @DisplayName("Current user is fetched on unfollow")
  void unfollow_should_fetch_current_user() {
    // given
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(ArtistEntityFactory.withExternalId(EXTERNAL_ID)));

    // when
    underTest.unfollow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(currentUserSupplier).get();
  }

  @Test
  @DisplayName("FollowAction is removed on unfollow")
  void unfollow_should_remove_artist_from_user() {
    // given
    ArtistEntity artist = ArtistEntityFactory.withExternalId(EXTERNAL_ID);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(artist));
    when(currentUserSupplier.get()).thenReturn(userEntity);

    // when
    underTest.unfollow(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(followActionRepository).deleteByUserAndArtist(userEntity, artist);
  }

  @Test
  @DisplayName("isCurrentUserFollowing(): should fetch user entity")
  void isCurrentUserFollowing_should_fetch_user_entity() {
    // given
    doReturn(userEntity).when(currentUserSupplier).get();
    ArtistEntity artist = ArtistEntityFactory.withExternalId(EXTERNAL_ID);
    when(artistRepository.findByExternalIdAndSource(anyString(), any())).thenReturn(Optional.of(artist));

    // when
    underTest.isCurrentUserFollowing(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(currentUserSupplier).get();
  }

  @Test
  @DisplayName("isCurrentUserFollowing(): should fetch artist entity")
  void isCurrentUserFollowing_should_fetch_artist_entity() {
    // when
    underTest.isCurrentUserFollowing(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(artistRepository).findByExternalIdAndSource(EXTERNAL_ID, ARTIST_SOURCE);
  }

  @Test
  @DisplayName("isCurrentUserFollowing(): should return false if artist entity does not exist")
  void isCurrentUserFollowing_should_return_false() {
    // when
    boolean result = underTest.isCurrentUserFollowing(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    assertThat(result).isFalse();
  }

  @Test
  @DisplayName("isCurrentUserFollowing(): should call FollowActionRepository if artist entity exists")
  void isCurrentUserFollowing_should_call_follow_action_repository() {
    var artistEntity = mock(ArtistEntity.class);
    doReturn(userEntity).when(currentUserSupplier).get();
    doReturn(Optional.of(artistEntity)).when(artistRepository).findByExternalIdAndSource(any(), any());

    // when
    underTest.isCurrentUserFollowing(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    verify(followActionRepository).existsByUserAndArtist(userEntity, artistEntity);
  }

  @ParameterizedTest(name = "should return {0}")
  @ValueSource(booleans = {true, false})
  @DisplayName("isCurrentUserFollowing(): should return result from FollowActionRepository")
  void isCurrentUserFollowing_should_result_from_follow_action_repository(boolean existsByUserIdAndArtistId) {
    doReturn(userEntity).when(currentUserSupplier).get();
    doReturn(Optional.of(mock(ArtistEntity.class))).when(artistRepository).findByExternalIdAndSource(any(), any());
    doReturn(existsByUserIdAndArtistId).when(followActionRepository).existsByUserAndArtist(any(), any());

    // when
    boolean result = underTest.isCurrentUserFollowing(EXTERNAL_ID, ARTIST_SOURCE);

    // then
    assertThat(result).isEqualTo(existsByUserIdAndArtistId);
  }

  @Test
  @DisplayName("Getting followed artists should get current user")
  void get_followed_should_call_user_supplier() {
    // when
    underTest.getFollowedArtistsOfCurrentUser();

    // then
    verify(currentUserSupplier).get();
  }

  @Test
  @DisplayName("Getting followed artists should call FollowActionRepository to fetch all follow actions")
  void get_followed_should_call_follow_action_repository() {
    // given
    doReturn(userEntity).when(currentUserSupplier).get();

    // when
    underTest.getFollowedArtistsOfCurrentUser();

    // then
    verify(followActionRepository).findAllByUser(userEntity);
  }

  @Test
  @DisplayName("Getting followed artists calls artist transformer for every artist")
  void get_followed_should_call_artist_transformer() {
    // given
    FollowActionEntity followAction1 = mock(FollowActionEntity.class);
    FollowActionEntity followAction2 = mock(FollowActionEntity.class);
    when(followActionRepository.findAllByUser(any())).thenReturn(List.of(followAction1, followAction2));
    when(artistDtoTransformer.transformFollowActionEntity(any(FollowActionEntity.class))).thenReturn(ArtistDtoFactory.createDefault());

    // when
    underTest.getFollowedArtistsOfCurrentUser();

    // then
    verify(artistDtoTransformer).transformFollowActionEntity(followAction1);
    verify(artistDtoTransformer).transformFollowActionEntity(followAction2);
  }

  @Test
  @DisplayName("Getting followed artists returns a sorted list of artist dtos")
  void get_followed_should_return_sorted_artist_dtos() {
    // given
    FollowActionEntity followAction1 = mock(FollowActionEntity.class);
    FollowActionEntity followAction2 = mock(FollowActionEntity.class);
    FollowActionEntity followAction3 = mock(FollowActionEntity.class);
    ArtistDto artistDto1 = ArtistDtoFactory.withName("Darkthrone");
    ArtistDto artistDto2 = ArtistDtoFactory.withName("Borknagar");
    ArtistDto artistDto3 = ArtistDtoFactory.withName("Alcest");

    when(followActionRepository.findAllByUser(any())).thenReturn(List.of(followAction1, followAction2, followAction3));
    when(artistDtoTransformer.transformFollowActionEntity(followAction1)).thenReturn(artistDto1);
    when(artistDtoTransformer.transformFollowActionEntity(followAction2)).thenReturn(artistDto2);
    when(artistDtoTransformer.transformFollowActionEntity(followAction3)).thenReturn(artistDto3);

    // when
    List<ArtistDto> followedArtists = underTest.getFollowedArtistsOfCurrentUser();

    // then
    assertThat(followedArtists).hasSize(3);
    assertThat(followedArtists.get(0)).isEqualTo(artistDto3);
    assertThat(followedArtists.get(1)).isEqualTo(artistDto2);
    assertThat(followedArtists.get(2)).isEqualTo(artistDto1);
  }

  @Test
  @DisplayName("artistService is called to find new artist ids")
  void test_artist_service_called_to_find_new_artists() {
    // given
    var artistIds = List.of("a", "b");

    // when
    underTest.followSpotifyArtists(artistIds);

    // then
    verify(artistService).findNewArtistIds(artistIds);
  }

  @Test
  @DisplayName("spotifyService is called to fetch new artists from Spotify")
  void test_spotify_service_called() {
    // given
    var newArtistIds = List.of("a", "b");
    doReturn(newArtistIds).when(artistService).findNewArtistIds(any());

    // when
    underTest.followSpotifyArtists(Collections.emptyList());

    // then
    verify(spotifyService).searchArtistsByIds(newArtistIds);
  }

  @Test
  @DisplayName("artistService is called with result from spotifyService to persist new artists")
  void test_artist_service_called_to_persist() {
    // given
    var newArtists = List.of(SpotifyArtistDtoFactory.createDefault());
    doReturn(newArtists).when(spotifyService).searchArtistsByIds(any());

    // when
    underTest.followSpotifyArtists(Collections.emptyList());

    // then
    verify(artistService).persistSpotifyArtists(newArtists);
  }

  @Test
  @DisplayName("artistRepository is called to get ArtistEntities")
  void test_artist_repository_called() {
    // given
    var artistIds = List.of("a", "b");

    // when
    underTest.followSpotifyArtists(artistIds);

    // then
    verify(artistRepository).findAllByExternalIdIn(artistIds);
  }

  @Test
  @DisplayName("Current user is fetched on follow multiple spotify artist")
  void test_follow_multiple_spotify_artists_should_get_current_user() {
    // when
    underTest.followSpotifyArtists(Collections.emptyList());

    // then
    verify(currentUserSupplier).get();
  }

  @Test
  @DisplayName("followActionRepository is called to save follow entities")
  void test_action_repository_is_called() {
    // given
    var artistEntity = ArtistEntityFactory.withExternalId("a");
    var expectedFollowActionEntities = List.of(FollowActionEntity.builder().artist(artistEntity).user(userEntity).build());
    doReturn(userEntity).when(currentUserSupplier).get();
    doReturn(List.of(artistEntity)).when(artistRepository).findAllByExternalIdIn(any());

    // when
    underTest.followSpotifyArtists(Collections.emptyList());

    // then
    verify(followActionRepository).saveAll(expectedFollowActionEntities);
  }
}
