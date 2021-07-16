package rocks.metaldetector.persistence.domain.artist;

import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import rocks.metaldetector.persistence.BaseDataJpaTest;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.persistence.domain.user.UserFactory;
import rocks.metaldetector.persistence.domain.user.UserRepository;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static rocks.metaldetector.persistence.domain.artist.ArtistSource.DISCOGS;
import static rocks.metaldetector.persistence.domain.artist.ArtistSource.SPOTIFY;

class ArtistRepositoryIT extends BaseDataJpaTest implements WithAssertions {

  @Autowired
  private ArtistRepository underTest;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private FollowActionRepository followActionRepository;

  private final ArtistEntity artist1 = ArtistEntityFactory.createArtistEntity("1", "1", SPOTIFY);
  private final ArtistEntity artist2 = ArtistEntityFactory.createArtistEntity("2", "2", SPOTIFY);
  private final ArtistEntity artist3 = ArtistEntityFactory.createArtistEntity("3", "3", DISCOGS);
  private final ArtistEntity artist4 = ArtistEntityFactory.createArtistEntity("4", "4", SPOTIFY);
  private final ArtistEntity artist5 = ArtistEntityFactory.createArtistEntity("5", "5", SPOTIFY);

  private final UserEntity userA = UserFactory.createUser("A", "a@test.com");
  private final UserEntity userB = UserFactory.createUser("B", "b@test.com");
  private final UserEntity userC = UserFactory.createUser("C", "c@test.com");

  @BeforeEach
  void setUp() {
    underTest.save(artist1);
    underTest.save(artist2);
    underTest.save(artist3);
    underTest.save(artist4);
    underTest.save(artist5);

    userRepository.save(userA);
    userRepository.save(userB);
    userRepository.save(userC);
  }

  @AfterEach
  void tearDown() {
    followActionRepository.deleteAll();
    underTest.deleteAll();
    userRepository.deleteAll();
  }

  @ParameterizedTest(name = "[{index}] => Entity <{0}>")
  @MethodSource("artistDetailsProvider")
  @DisplayName("findByExternalIdAndSource() finds the correct entity for a given artist id and source if it exists")
  void find_by_artist_external_id_should_return_correct_entity(String artistId, ArtistSource source) {
    // when
    Optional<ArtistEntity> artistEntityOptional = underTest.findByExternalIdAndSource(artistId, source);

    // then
    assertThat(artistEntityOptional).isPresent();
    assertThat(artistEntityOptional.get().getExternalId()).isEqualTo(artistId);
    assertThat(artistEntityOptional.get().getArtistName()).isEqualTo(String.valueOf(artistId));
  }

  @Test
  @DisplayName("findByExternalIdAndSource() returns empty optional if given artist does not exist")
  void find_by_artist_external_id_should_return_empty_optional() {
    // when
    Optional<ArtistEntity> artistEntityOptional = underTest.findByExternalIdAndSource("0", SPOTIFY);

    // then
    assertThat(artistEntityOptional).isEmpty();
  }

  @ParameterizedTest(name = "[{index}] => Entity <{0}>")
  @MethodSource("artistDetailsProvider")
  @DisplayName("existsByExternalIdAndSource() should return true if artist exists")
  void exists_by_artist_external_id_should_return_true(String entity, ArtistSource source) {
    // when
    boolean exists = underTest.existsByExternalIdAndSource(entity, source);

    // then
    assertThat(exists).isTrue();
  }

  @Test
  @DisplayName("existsByExternalIdAndSource() should return false if artist does not exist")
  void exists_by_artist_external_id_should_return_false() {
    // when
    boolean exists = underTest.existsByExternalIdAndSource("0", SPOTIFY);

    // then
    assertThat(exists).isFalse();
  }

  @Test
  @DisplayName("findAllByArtistExternalIds() should return correct entities if they exist")
  void find_all_by_artist_external_ids_should_return_correct_entities() {
    // when
    List<ArtistEntity> artistEntities = underTest.findAllByExternalIdIn(List.of("0", "1", "2"));

    // then
    assertThat(artistEntities).hasSize(2);

    for (int i = 0; i < artistEntities.size(); i++) {
      ArtistEntity entity = artistEntities.get(i);
      assertThat(entity.getArtistName()).isEqualTo(String.valueOf(i + 1));
      assertThat(entity.getExternalId()).isEqualTo(String.valueOf(i + 1));
    }
  }

  @ParameterizedTest(name = "[{index}] => ExternalIds <{0}>")
  @MethodSource("inputProviderExternalIds")
  @DisplayName("findAllByArtistExternalIds() should return correct entities if they exist")
  void find_all_by_artist_external_ids_should_return_correct_entities_parametrized(Collection<String> externalIds) {
    // when
    List<ArtistEntity> artistEntities = underTest.findAllByExternalIdIn(externalIds);

    // then
    assertThat(artistEntities).hasSize(externalIds.size());

    for (int i = 0; i < externalIds.size(); i++) {
      ArtistEntity entity = artistEntities.get(i);
      assertThat(entity.getArtistName()).isEqualTo(String.valueOf(i + 1));
      assertThat(entity.getExternalId()).isEqualTo(String.valueOf(i + 1));
    }
  }

  @Test
  @DisplayName("findTopArtists() finds the most followed artists in the correct order")
  void test_find_top_artists() {
    // given
    int minFollower = 2;
    int expectedSize = 2;
    follow(userA, artist1);
    follow(userA, artist2);
    follow(userA, artist3);
    follow(userB, artist2);
    follow(userB, artist3);
    follow(userC, artist3);

    // when
    var result = underTest.findTopArtists(minFollower);

    // then
    assertThat(result).hasSize(expectedSize);
    assertThat(result.get(0).getArtistName()).isEqualTo(artist3.getArtistName());
    assertThat(result.get(0).getImageXs()).isEqualTo(artist3.getImageXs());
    assertThat(result.get(0).getImageS()).isEqualTo(artist3.getImageS());
    assertThat(result.get(0).getImageM()).isEqualTo(artist3.getImageM());
    assertThat(result.get(0).getImageL()).isEqualTo(artist3.getImageL());
    assertThat(result.get(0).getExternalId()).isEqualTo(artist3.getExternalId());
    assertThat(result.get(0).getSource().getDisplayName()).isEqualTo(artist3.getSource().getDisplayName());

    assertThat(result.get(1).getArtistName()).isEqualTo(artist2.getArtistName());
    assertThat(result.get(1).getImageXs()).isEqualTo(artist2.getImageXs());
    assertThat(result.get(1).getImageS()).isEqualTo(artist2.getImageS());
    assertThat(result.get(1).getImageM()).isEqualTo(artist2.getImageM());
    assertThat(result.get(1).getImageL()).isEqualTo(artist2.getImageL());
    assertThat(result.get(1).getExternalId()).isEqualTo(artist2.getExternalId());
    assertThat(result.get(1).getSource().getDisplayName()).isEqualTo(artist2.getSource().getDisplayName());
  }

  @Test
  @DisplayName("countArtistFollower() count the users that follow the given artist")
  void test_count_artist_follower() {
    // given
    follow(userA, artist3);
    follow(userB, artist3);
    follow(userC, artist3);

    // when
    var result = underTest.countArtistFollower(artist3.getExternalId());

    // then
    assertThat(result).isEqualTo(3);
  }

  private static Stream<Arguments> artistDetailsProvider() {
    return Stream.of(
            Arguments.of("1", SPOTIFY),
            Arguments.of("2", SPOTIFY),
            Arguments.of("3", DISCOGS)
    );
  }

  private static Stream<Arguments> inputProviderExternalIds() {
    return Stream.of(
        Arguments.of(List.of("2", "1")),
        Arguments.of(List.of("1")),
        Arguments.of(Collections.emptyList())
    );
  }

  private void follow(UserEntity user, ArtistEntity artist) {
    followActionRepository.save(FollowActionEntity.builder().user(user).artist(artist).build());
  }
}
