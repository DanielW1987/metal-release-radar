package rocks.metaldetector.service.summary;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rocks.metaldetector.persistence.domain.artist.ArtistRepository;
import rocks.metaldetector.persistence.domain.artist.FollowActionEntity;
import rocks.metaldetector.persistence.domain.artist.FollowActionRepository;
import rocks.metaldetector.persistence.domain.artist.TopArtist;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.persistence.domain.user.UserRepository;
import rocks.metaldetector.security.CurrentPublicUserIdSupplier;
import rocks.metaldetector.service.artist.ArtistEntityFactory;
import rocks.metaldetector.service.artist.ArtistTransformer;
import rocks.metaldetector.service.user.UserEntityFactory;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;
import rocks.metaldetector.testutil.DtoFactory.ArtistDtoFactory;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static rocks.metaldetector.service.summary.SummaryServiceImpl.RESULT_LIMIT;

@ExtendWith(MockitoExtension.class)
class ArtistCollectorTest implements WithAssertions {

  @Mock
  private ArtistRepository artistRepository;

  @Mock
  private ArtistTransformer artistTransformer;

  @Mock
  private CurrentPublicUserIdSupplier currentPublicUserIdSupplier;

  @Mock
  private UserRepository userRepository;

  @Mock
  private FollowActionRepository followActionRepository;

  @InjectMocks
  private ArtistCollector underTest;

  private final UserEntity userEntity = UserEntityFactory.createUser("user", "user@mail.com");

  @AfterEach
  void tearDown() {
    reset(artistRepository, artistTransformer, currentPublicUserIdSupplier, userRepository, followActionRepository);
  }

  @Test
  @DisplayName("collectTopFollowedArtists: artistRepository is called to get top artists")
  void test_artist_repository_is_called_for_top_artists() {
    // when
    underTest.collectTopFollowedArtists();

    // then
    verify(artistRepository, times(1)).findTopArtists(RESULT_LIMIT);
  }

  @Test
  @DisplayName("collectTopFollowedArtists: artistTransformer is called for each artist")
  void test_artist_transformer_is_called_for_artists() {
    // given
    var topArtists = List.of(mock(TopArtist.class), mock(TopArtist.class));
    var artistDto = ArtistDtoFactory.createDefault();
    doReturn(topArtists).when(artistRepository).findTopArtists(anyInt());
    doReturn(artistDto).when(artistTransformer).transform(any(TopArtist.class));

    // when
    underTest.collectTopFollowedArtists();

    // then
    verify(artistTransformer, times(1)).transform(topArtists.get(0));
    verify(artistTransformer, times(1)).transform(topArtists.get(1));
  }

  @Test
  @DisplayName("collectTopFollowedArtists: artist dtos are returned")
  void test_artist_dtos_are_returned() {
    // given
    var artistEntities = List.of(mock(TopArtist.class), mock(TopArtist.class));
    doReturn(artistEntities).when(artistRepository).findTopArtists(anyInt());
    var expectedArtistDtos = List.of(ArtistDtoFactory.withName("A"), ArtistDtoFactory.withName("B"));
    doReturn(expectedArtistDtos.get(0)).when(artistTransformer).transform(artistEntities.get(0));
    doReturn(expectedArtistDtos.get(1)).when(artistTransformer).transform(artistEntities.get(1));

    // when
    var result = underTest.collectTopFollowedArtists();

    // then
    assertThat(result).isEqualTo(expectedArtistDtos);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: currentPublicUserIdSupplier is called")
  void test_current_user_id_supplier_called() {
    // given
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());

    // when
    underTest.collectRecentlyFollowedArtists();

    // then
    verify(currentPublicUserIdSupplier, times(1)).get();
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: userRepository is called")
  void test_user_repository_called() {
    // given
    var userId = "userId";
    doReturn(userId).when(currentPublicUserIdSupplier).get();
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());

    // when
    underTest.collectRecentlyFollowedArtists();

    // then
    verify(userRepository, times(1)).findByPublicId(userId);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: userRepository throws exception when publicUserId not found")
  void test_user_repository_throws_exception() {
    // given
    var publicUserId = "publicUserId";
    doThrow(new ResourceNotFoundException(publicUserId)).when(userRepository).findByPublicId(any());

    // when
    Throwable throwable = catchThrowable(() -> underTest.collectRecentlyFollowedArtists());

    // then
    assertThat(throwable).isInstanceOf(ResourceNotFoundException.class);
    assertThat(throwable).hasMessageContaining(publicUserId);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: followActionRepository is called with user")
  void test_follow_action_repository_called() {
    // given
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());

    // when
    underTest.collectRecentlyFollowedArtists();

    // then
    verify(followActionRepository, times(1)).findAllByUser(userEntity);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: artistTransformer is called for every followActionEntity")
  void test_artist_transformer_called() {
    // given
    var artist1 = ArtistEntityFactory.withExternalId("1");
    var artist2 = ArtistEntityFactory.withExternalId("2");
    var userFollowsArtist1 = FollowActionEntity.builder().user(userEntity).artist(artist1).build();
    var userFollowsArtist2 = FollowActionEntity.builder().user(userEntity).artist(artist2).build();
    var followActionEntities = List.of(userFollowsArtist1, userFollowsArtist2);
    followActionEntities.forEach(action -> action.setCreatedDateTime(Date.from(Instant.now())));
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());
    doReturn(followActionEntities).when(followActionRepository).findAllByUser(any());

    // when
    underTest.collectRecentlyFollowedArtists();

    // then
    verify(artistTransformer, times(1)).transform(userFollowsArtist1);
    verify(artistTransformer, times(1)).transform(userFollowsArtist2);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: dtos are sorted with descending following date")
  void test_dtos_sorted_by_reversed_created_date() {
    // given
    var artist1 = ArtistEntityFactory.withExternalId("1");
    var artist2 = ArtistEntityFactory.withExternalId("2");
    var userFollowsArtist1 = FollowActionEntity.builder().user(userEntity).artist(artist1).build();
    var userFollowsArtist2 = FollowActionEntity.builder().user(userEntity).artist(artist2).build();
    userFollowsArtist1.setCreatedDateTime(Date.from(Instant.now().minus(1, DAYS)));
    userFollowsArtist2.setCreatedDateTime(Date.from(Instant.now()));
    var followActionEntities = List.of(userFollowsArtist1, userFollowsArtist2);
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());
    doReturn(followActionEntities).when(followActionRepository).findAllByUser(any());
    InOrder inOrder = inOrder(artistTransformer);

    // when
    underTest.collectRecentlyFollowedArtists();

    // then
    inOrder.verify(artistTransformer, times(1)).transform(userFollowsArtist2);
    inOrder.verify(artistTransformer, times(1)).transform(userFollowsArtist1);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: result size is limited")
  void test_result_limited() {
    // given
    var artist = ArtistEntityFactory.withExternalId("1");
    var followActionEntities = IntStream.rangeClosed(1, RESULT_LIMIT + 1)
        .mapToObj(value -> FollowActionEntity.builder().user(userEntity).artist(artist).build())
        .peek(action -> action.setCreatedDateTime(Date.from(Instant.now())))
        .collect(Collectors.toList());
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());
    doReturn(followActionEntities).when(followActionRepository).findAllByUser(any());

    // when
    var result = underTest.collectRecentlyFollowedArtists();

    // then
    assertThat(result).hasSize(RESULT_LIMIT);
  }

  @Test
  @DisplayName("collectRecentlyFollowedArtists: transformed artistDtos are returned")
  void test_dtos_are_returned() {
    // given
    var artist = ArtistEntityFactory.withExternalId("1");
    var followAction = FollowActionEntity.builder().user(userEntity).artist(artist).build();
    var expectedArtist = ArtistDtoFactory.createDefault();
    doReturn(Optional.of(userEntity)).when(userRepository).findByPublicId(any());
    doReturn(List.of(followAction)).when(followActionRepository).findAllByUser(any());
    doReturn(expectedArtist).when(artistTransformer).transform(followAction);

    // when
    var result = underTest.collectRecentlyFollowedArtists();

    // then
    assertThat(result).containsExactly(expectedArtist);
  }
}
