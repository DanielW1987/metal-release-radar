package rocks.metaldetector.discogs.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import rocks.metaldetector.discogs.api.DiscogsArtist;
import rocks.metaldetector.discogs.api.DiscogsArtistSearchResultContainer;
import rocks.metaldetector.discogs.api.DiscogsPagination;
import rocks.metaldetector.discogs.config.DiscogsCredentialsConfig;
import rocks.metaldetector.support.ExternalServiceException;

import java.util.Collections;

@Slf4j
@Service
@Profile({"default", "preview", "prod"})
public class DiscogsArtistSearchRestClientImpl implements DiscogsArtistSearchRestClient {

  private static final DiscogsArtistSearchResultContainer EMPTY_RESPONSE = createEmptyResponse();

  static final String ARTIST_NAME_SEARCH_URL_FRAGMENT = "/database/search?type=artist&q={artistQueryString}&page={page}&per_page={size}";
  static final String ARTIST_ID_SEARCH_URL_FRAGMENT = "/artists/{artistId}";

  private final RestTemplate restTemplate;
  private final DiscogsCredentialsConfig discogsCredentialsConfig;

  @Autowired
  public DiscogsArtistSearchRestClientImpl(RestTemplate restTemplate, DiscogsCredentialsConfig discogsCredentialsConfig) {
    this.restTemplate = restTemplate;
    this.discogsCredentialsConfig = discogsCredentialsConfig;
  }

  @Override
  public DiscogsArtistSearchResultContainer searchByName(String artistQueryString, int pageNumber, int pageSize) {
    if (artistQueryString == null || artistQueryString.isEmpty()) {
      return EMPTY_RESPONSE;
    }

    ResponseEntity<DiscogsArtistSearchResultContainer> responseEntity = restTemplate.getForEntity(
            discogsCredentialsConfig.getRestBaseUrl() + ARTIST_NAME_SEARCH_URL_FRAGMENT,
            DiscogsArtistSearchResultContainer.class,
            artistQueryString,
            pageNumber,
            pageSize
    );

    DiscogsArtistSearchResultContainer resultContainer = responseEntity.getBody();
    var shouldNotHappen = resultContainer == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not get search results for query '" + artistQueryString + "' (Response code: " + responseEntity.getStatusCode() + ")");
    }

    return resultContainer;
  }

  @Override
  public DiscogsArtist searchById(long artistId) {
    if (artistId <= 0) {
      throw new IllegalArgumentException("artistId must not be negative!");
    }

    ResponseEntity<DiscogsArtist> responseEntity = restTemplate.getForEntity(
            discogsCredentialsConfig.getRestBaseUrl() + ARTIST_ID_SEARCH_URL_FRAGMENT,
            DiscogsArtist.class,
            artistId
    );

    DiscogsArtist discogsArtist = responseEntity.getBody();
    var shouldNotHappen = discogsArtist == null || !responseEntity.getStatusCode().is2xxSuccessful();
    if (shouldNotHappen) {
      throw new ExternalServiceException("Could not get artist for artistId '" + artistId + "' (Response code: " + responseEntity.getStatusCode() + ")");
    }

    return discogsArtist;
  }

  private static DiscogsArtistSearchResultContainer createEmptyResponse() {
    return DiscogsArtistSearchResultContainer.builder()
            .pagination(new DiscogsPagination())
            .results(Collections.emptyList())
            .build();
  }
}