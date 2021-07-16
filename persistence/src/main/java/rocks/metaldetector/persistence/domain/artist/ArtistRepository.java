package rocks.metaldetector.persistence.domain.artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

  Optional<ArtistEntity> findByExternalIdAndSource(String externalId, ArtistSource source);

  List<ArtistEntity> findAllByExternalIdIn(Collection<String> externalIds);

  boolean existsByExternalIdAndSource(String externalId, ArtistSource source);

  @Query(value = "select a.artist_name as artistName, a.external_id as externalId, a.source as source," +
                 "a.image_xs as imageXs, a.image_s as imageS, a.image_m as imageM, a.image_l as imageL " +
                 "from follow_actions as fa left join artists as a on a.id = fa.artist_id " +
                 "group by a.artist_name, a.external_id, a.source, a.image_xs, a.image_s, a.image_m, a.image_l " +
                 "having count(a.id) >= :minFollower " +
                 "order by count(a.id) desc",
         nativeQuery = true)
  List<TopArtist> findTopArtists(@Param("minFollower") int minFollower);

  @Query(value = "select count(a.external_id) " +
                 "from follow_actions fa left join artists a on a.id = fa.artist_id " +
                 "where a.external_id = :externalId",
         nativeQuery = true)
  int countArtistFollower(@Param("externalId") String externalId);
}
