package com.metalr2.web.controller;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.model.user.UserEntity;
import com.metalr2.security.CurrentUserSupplier;
import com.metalr2.web.DtoFactory;
import com.metalr2.web.RestAssuredRequestHandler;
import com.metalr2.web.controller.discogs.DiscogsArtistSearchRestClientImpl;
import com.metalr2.web.dto.request.ArtistDetailsRequest;
import com.metalr2.web.dto.response.ArtistDetailsResponse;
import com.metalr2.web.dto.response.ErrorResponse;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-test.properties")
@Tag("integration-test")
@ExtendWith(MockitoExtension.class)
class ArtistDetailsRestControllerIT implements WithAssertions {

  private static final String USER_ID = "TestId";

  @MockBean
  private DiscogsArtistSearchRestClientImpl artistSearchClient;

  @MockBean
  private CurrentUserSupplier currentUserSupplier;

  @Mock
  private UserEntity userEntity;

  @Value("${server.address}")
  private String serverAddress;

  @LocalServerPort
  private int port;

  private RestAssuredRequestHandler<ArtistDetailsRequest> requestHandler;

  @BeforeEach
  void setUp() {
    String requestUri   = "http://" + serverAddress + ":" + port + Endpoints.Rest.ARTIST_DETAILS_V1;
    requestHandler      = new RestAssuredRequestHandler<>(requestUri);

  }

  @AfterEach
  void tearDown() {
  }

  @Test
  @DisplayName("POST with valid request should return 200")
  void post_with_valid_request_should_return_200() {
    ArtistDetailsRequest artistDetailsRequest = new ArtistDetailsRequest("Testname",1L);

    when(artistSearchClient.searchById(artistDetailsRequest.getArtistId()))
            .thenReturn(Optional.of(DtoFactory.ArtistFactory.createTestArtist()));
    when(currentUserSupplier.get()).thenReturn(userEntity);
    when(userEntity.getPublicId()).thenReturn(USER_ID);

    ValidatableResponse validatableResponse = requestHandler.doPost(ContentType.JSON, artistDetailsRequest);

    // assert
    validatableResponse.contentType(ContentType.JSON)
            .statusCode(HttpStatus.OK.value());

    ArtistDetailsResponse artistDetailsResponse = validatableResponse.extract().as(ArtistDetailsResponse.class);

    assertThat(artistDetailsResponse).isNotNull();
    assertThat(artistDetailsResponse.getArtistId()).isEqualTo(1L);

    verify(artistSearchClient,times(1)).searchById(artistDetailsRequest.getArtistId());
  }

  @Test
  @DisplayName("POST with bad request should return 400")
  void post_with_bad_request_should_return_400() {
    ArtistDetailsRequest badArtistDetailsRequest = new ArtistDetailsRequest(null,null);

    ValidatableResponse validatableResponse = requestHandler.doPost(ContentType.JSON, badArtistDetailsRequest);
    ErrorResponse errorResponse = validatableResponse.extract().as(ErrorResponse.class);

    // assert
    validatableResponse.statusCode(HttpStatus.BAD_REQUEST.value())
            .contentType(ContentType.JSON);

    assertThat(errorResponse).isNotNull();
    assertThat(errorResponse.getMessages()).hasSize(2);
  }

  @Test
  @DisplayName("POST with not results should return 404")
  void post_with_no_results_should_return_404() {
    ArtistDetailsRequest artistDetailsRequest = new ArtistDetailsRequest("Testname",0L);

    when(artistSearchClient.searchById(artistDetailsRequest.getArtistId()))
            .thenReturn(Optional.empty());

    ValidatableResponse validatableResponse = requestHandler.doPost(ContentType.JSON, artistDetailsRequest);

    // assert
    validatableResponse.statusCode(HttpStatus.NOT_FOUND.value());

    verify(artistSearchClient,times(1)).searchById(0L);
  }

}