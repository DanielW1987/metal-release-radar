package rocks.metaldetector.butler.facade;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rocks.metaldetector.butler.api.ButlerReleasesRequest;
import rocks.metaldetector.butler.api.ButlerReleasesResponse;
import rocks.metaldetector.butler.client.ReleaseButlerRestClient;
import rocks.metaldetector.butler.client.transformer.ButlerReleaseRequestTransformer;
import rocks.metaldetector.butler.client.transformer.ButlerReleaseResponseTransformer;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReleaseServiceImpl implements ReleaseService {

  private final ReleaseButlerRestClient butlerClient;
  private final ButlerReleaseRequestTransformer requestTransformer;
  private final ButlerReleaseResponseTransformer responseTransformer;

  @Autowired
  public ReleaseServiceImpl(ReleaseButlerRestClient butlerClient, ButlerReleaseRequestTransformer requestTransformer,
                            ButlerReleaseResponseTransformer responseTransformer) {
    this.butlerClient = butlerClient;
    this.requestTransformer = requestTransformer;
    this.responseTransformer = responseTransformer;
  }

  @Override
  public List<ReleaseDto> findReleases(Iterable<String> artists, LocalDate dateFrom, LocalDate dateTo) {
    ButlerReleasesRequest request = requestTransformer.transform(artists, dateFrom, dateTo);
    ButlerReleasesResponse response = butlerClient.queryReleases(request);
    return responseTransformer.transform(response);
  }
}