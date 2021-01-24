package rocks.metaldetector.butler.facade.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReleaseDto {

  private long id;
  private String artist;
  private List<String> additionalArtists;
  private String albumTitle;
  private LocalDate releaseDate;
  private LocalDate announcementDate;
  private String estimatedReleaseDate;
  private String genre;
  private String type;
  private String artistDetailsUrl;
  private String releaseDetailsUrl;
  private String source;
  private String state;
  private String coverUrl;

}
