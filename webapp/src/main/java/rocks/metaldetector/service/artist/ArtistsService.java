package rocks.metaldetector.service.artist;

import org.springframework.data.domain.Pageable;
import rocks.metaldetector.discogs.facade.dto.DiscogsArtistSearchResultDto;
import rocks.metaldetector.spotify.facade.dto.SpotifyArtistSearchResultDto;

import java.util.List;
import java.util.Optional;

public interface ArtistsService {

  Optional<ArtistDto> findArtistByDiscogsId(long discogsId);
  List<ArtistDto> findAllArtistsByDiscogsIds(long... discogsIds);
  boolean existsArtistByDiscogsId(long discogsId);

  DiscogsArtistSearchResultDto searchDiscogsByName(String artistQueryString, Pageable pageable);
  SpotifyArtistSearchResultDto searchSpotifyByName(String artistQueryString, Pageable pageable);

}
