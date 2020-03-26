package rocks.metaldetector.web.controller.rest;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.config.constants.Endpoints;
import rocks.metaldetector.service.artist.ArtistsService;
import rocks.metaldetector.testutil.WithIntegrationTestConfig;
import rocks.metaldetector.web.DtoFactory.DetectorReleaseRequestFactory;
import rocks.metaldetector.web.RestAssuredRequestHandler;
import rocks.metaldetector.web.api.request.DetectorReleasesRequest;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class ReleasesRestControllerIT implements WithAssertions, WithIntegrationTestConfig {

  @MockBean
  private ReleaseService releasesService;

  @MockBean
  private ArtistsService artistsService;

  @SpyBean
  ModelMapper modelMapper;

  @LocalServerPort
  private int port;

  @BeforeEach
  void setUp() {
    String requestUri = "http://localhost:" + port + Endpoints.Rest.RELEASES;
    requestHandler = new RestAssuredRequestHandler(requestUri);
  }

  @AfterEach
  void tearDown() {
    reset(releasesService, artistsService);
  }

  private RestAssuredRequestHandler requestHandler;

  @Test
  @DisplayName("Should return a list of releases")
  void should_return_releases() {
    // ToDo DanielW: Complete this
    // given
    List<ReleaseDto> releases = List.of(

    );
    when(releasesService.findReleases(any(), any(), any())).thenReturn(releases);

    // when
    ValidatableResponse validatableResponse = requestHandler.doPost(new DetectorReleasesRequest(), ContentType.JSON);

    // then
    validatableResponse
            .contentType(ContentType.JSON)
            .statusCode(HttpStatus.OK.value());
  }

  @Test
  @DisplayName("Should pass request parameter to release service")
  void should_pass_parameter_to_release_service() {
    // given
    DetectorReleasesRequest request = DetectorReleaseRequestFactory.createDefault();

    // when
    requestHandler.doPost(request, ContentType.JSON);

    // then
    verify(releasesService, times(1)).findReleases(request.getArtists(), request.getDateFrom(), request.getDateTo());
  }

  @Test
  @DisplayName("Should set all followed artists in response")
  void test() {
    // given


    // when
    // ToDo DanielW: Implement

    // then

  }

  // ToDo DanielW: Remove this
//  @ParameterizedTest(name = "[{index}] => ButlerRequest <{0}> | RestControllerRequest <{1}> | MockedDtos <{2}> | ExpectedResponses <{3}>")
//  @MethodSource("inputProvider")
//  @DisplayName("POST should return valid results")
//  void post_valid_result(ButlerReleasesRequest requestButler, DetectorReleasesRequest request, List<ReleaseDto> mockedDtos, List<DetectorReleasesResponse> expectedResponses) {
//    // given
//    when(releasesService.findReleases(requestButler)).thenReturn(mockedDtos);
//    when(artistsService.findFollowedArtistsForCurrentUser()).thenReturn(Collections.emptyList());
//
//    for (int i = 0; i < mockedDtos.size(); i++) {
//      when(modelMapper.map(mockedDtos.get(i), DetectorReleasesResponse.class)).thenReturn(expectedResponses.get(i));
//    }
//
//    // when
//    ValidatableResponse validatableResponse = requestHandler.doPost(request, ContentType.JSON);
//
//    // then
//    validatableResponse
//        .contentType(ContentType.JSON)
//        .statusCode(HttpStatus.OK.value());
//
//    List<DetectorReleasesResponse> response = validatableResponse.extract().body().jsonPath().getList(".", DetectorReleasesResponse.class);
//
//    assertThat(response).isEqualTo(expectedResponses);
//    verify(modelMapper, times(mockedDtos.size())).map(any(), eq(DetectorReleasesResponse.class));
//  }

  @Test
  @DisplayName("POST with bad requests should return 400")
  void bad_requests() {
    // given
    DetectorReleasesRequest request = new DetectorReleasesRequest(null, null, null);

    // when
    ValidatableResponse validatableResponse = requestHandler.doPost(request, ContentType.JSON);

    // then
    validatableResponse
        .contentType(ContentType.JSON)
        .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  // ToDo DanielW: Remove this
//  private static Stream<Arguments> inputProvider() {
//    LocalDate date = LocalDate.now();
//    ReleaseDto releaseDto1 = ReleaseDtoFactory.withOneResult("A1", date);
//    ReleaseDto releaseDto2 = ReleaseDtoFactory.withOneResult("A2", date);
//    ReleaseDto releaseDto3 = ReleaseDtoFactory.withOneResult("A3", date);
//
//    DetectorReleasesResponse releaseResponse1 = DetectorReleaseResponseFactory.withOneResult("A1", date);
//    DetectorReleasesResponse releaseResponse2 = DetectorReleaseResponseFactory.withOneResult("A2", date);
//    DetectorReleasesResponse releaseResponse3 = DetectorReleaseResponseFactory.withOneResult("A3", date);
//
//    ButlerReleasesRequest requestAllButler = new ButlerReleasesRequest(null, null, Collections.singletonList("A1"));
//    DetectorReleasesRequest requestAll = new DetectorReleasesRequest(null, null, Collections.emptyList());
//
//    ButlerReleasesRequest requestA1Butler = new ButlerReleasesRequest(null, null, Collections.singletonList("A1"));
//    DetectorReleasesRequest requestA1 = new DetectorReleasesRequest(null, null, Collections.emptyList());
//
//    ButlerReleasesRequest requestA4Butler = new ButlerReleasesRequest(null, null, Collections.singletonList("A1"));
//    DetectorReleasesRequest requestA4 = new DetectorReleasesRequest(null, null, Collections.emptyList());
//
//    return Stream.of(
//        Arguments.of(requestAllButler, requestAll, List.of(releaseDto1, releaseDto2, releaseDto3), List.of(releaseResponse1, releaseResponse2, releaseResponse3)),
//        Arguments.of(requestA1Butler, requestA1, List.of(releaseDto1), List.of(releaseResponse1)),
//        Arguments.of(requestA4Butler, requestA4, Collections.emptyList(), Collections.emptyList()));
//  }
}