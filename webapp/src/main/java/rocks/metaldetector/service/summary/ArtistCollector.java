package rocks.metaldetector.service.summary;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import rocks.metaldetector.persistence.domain.artist.ArtistRepository;
import rocks.metaldetector.persistence.domain.artist.FollowActionEntity;
import rocks.metaldetector.persistence.domain.artist.FollowActionRepository;
import rocks.metaldetector.persistence.domain.user.AbstractUserEntity;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.service.artist.transformer.ArtistDtoTransformer;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class ArtistCollector {

  private final ArtistRepository artistRepository;
  private final ArtistDtoTransformer artistDtoTransformer;
  private final FollowActionRepository followActionRepository;
  private final CurrentUserSupplier currentUserSupplier;

  public List<ArtistDto> collectTopFollowedArtists(int minFollower) {
    return artistRepository.findTopArtists(minFollower).stream()
        .map(artistDtoTransformer::transformTopArtist)
        .peek(artist -> artist.setFollower(artistRepository.countArtistFollower(artist.getExternalId())))
        .collect(Collectors.toList());
  }

  public List<ArtistDto> collectRecentlyFollowedArtists(int resultLimit) {
    AbstractUserEntity currentUser = currentUserSupplier.get();
    return followActionRepository.findAllByUser(currentUser).stream()
        .sorted(Comparator.comparing(FollowActionEntity::getCreatedDateTime).reversed())
        .limit(resultLimit)
        .map(artistDtoTransformer::transformFollowActionEntity)
        .collect(Collectors.toList());
  }
}
