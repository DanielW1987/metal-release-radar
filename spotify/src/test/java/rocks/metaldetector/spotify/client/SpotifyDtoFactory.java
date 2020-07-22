package rocks.metaldetector.spotify.client;

import rocks.metaldetector.spotify.api.authentication.SpotifyAuthenticationResponse;
import rocks.metaldetector.spotify.api.search.SpotifyArtist;
import rocks.metaldetector.spotify.api.search.SpotifyArtistSearchResult;
import rocks.metaldetector.spotify.api.search.SpotifyArtistSearchResultContainer;
import rocks.metaldetector.spotify.api.search.SpotifyFollowers;
import rocks.metaldetector.spotify.api.search.SpotifyImage;

import java.util.Collections;
import java.util.List;

public class SpotifyDtoFactory {

  public static class SpotifyArtistSearchResultContainerFactory {

    public static SpotifyArtistSearchResultContainer createDefault() {
      return SpotifyArtistSearchResultContainer.builder()
          .artists(SpotifyArtistSearchResultFactory.createDefault())
          .build();
    }

    public static SpotifyArtistSearchResultContainer withIndivualPagination(int offset, int limit, int total) {
      return SpotifyArtistSearchResultContainer.builder()
          .artists(SpotifyArtistSearchResultFactory.withIndivualPagination(offset, limit, total))
          .build();
    }
  }

  static class SpotifyArtistSearchResultFactory {

    static SpotifyArtistSearchResult createDefault() {
      return SpotifyArtistSearchResult.builder()
          .href("query")
          .limit(10)
          .next("nextPageLink")
          .previous("previousPageLink")
          .offset(10)
          .total(20)
          .items(List.of(
              SpotfiyArtistFatory.withArtistName("A"),
              SpotfiyArtistFatory.withArtistName("B")
          ))
          .build();
    }

    static SpotifyArtistSearchResult withIndivualPagination(int offset, int limit, int total) {
      return SpotifyArtistSearchResult.builder()
          .href("query")
          .limit(limit)
          .next("nextPageLink")
          .previous("previousPageLink")
          .offset(offset)
          .total(total)
          .items(Collections.emptyList())
          .build();
    }
  }

  static class SpotfiyArtistFatory {

    static SpotifyArtist withArtistName(String artistName) {
      return SpotifyArtist.builder()
          .externalUrls(Collections.emptyMap())
          .followers(SpotfiyFollowersFatory.createDefault())
          .genres(List.of("Black Metal"))
          .href("artistLink")
          .id("abc")
          .images(List.of(SpotfiyImageFatory.createDefault()))
          .name(artistName)
          .popularity(100)
          .uri("artistUri")
          .build();
    }
  }

  static class SpotfiyFollowersFatory {

    static SpotifyFollowers createDefault() {
      return SpotifyFollowers.builder()
          .total(666)
          .href("link")
          .build();
    }
  }

  static class SpotfiyImageFatory {

    static SpotifyImage createDefault() {
      return SpotifyImage.builder()
          .height(150)
          .width(150)
          .url("link")
          .build();
    }
  }

  public static class SpotifyAuthenticationResponseFactory {

    public static SpotifyAuthenticationResponse createDefault() {
      return SpotifyAuthenticationResponse.builder()
          .accessToken("accessToken")
          .expiresIn(3600)
          .tokenType("tokenType")
          .build();
    }
  }
}
