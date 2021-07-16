package rocks.metaldetector.service.summary;

import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.web.api.response.SummaryResponse;

import java.util.List;

public interface SummaryService {

  SummaryResponse createSummaryResponse();

  List<ReleaseDto> findTopReleases(TimeRange timeRange, int minFollower, int maxReleases);
}
