package rocks.metaldetector.persistence.domain.artist;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<ArtistEntity, Long> {

  Optional<ArtistEntity> findByArtistDiscogsId(long artistDiscogsId);

  List<ArtistEntity> findAllByArtistDiscogsIdIn(long... artistDiscogsIds);

  boolean existsByArtistDiscogsId(long artistDiscogsId);

}