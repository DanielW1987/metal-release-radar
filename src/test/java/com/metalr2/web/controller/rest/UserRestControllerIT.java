package com.metalr2.web.controller.rest;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.model.exceptions.ResourceNotFoundException;
import com.metalr2.service.user.UserService;
import com.metalr2.testutil.WithIntegrationTestProfile;
import com.metalr2.web.DtoFactory;
import com.metalr2.web.RestAssuredRequestHandler;
import com.metalr2.web.dto.UserDto;
import com.metalr2.web.dto.response.UserResponse;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import org.assertj.core.api.WithAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension.class)
class UserRestControllerIT implements WithAssertions, WithIntegrationTestProfile {

  @MockBean
  private UserService userService;

  @SpyBean
  ModelMapper modelMapper;

  @LocalServerPort
  private int port;

  private RestAssuredRequestHandler requestHandler;

  @BeforeEach
  void setup() {
    requestHandler = new RestAssuredRequestHandler("http://localhost:" + port + Endpoints.Rest.USERS);
  }

  @DisplayName("Get all users tests")
  @Nested
  class GetAllUsersTest {

    @Test
    @DisplayName("Should return all users")
    void should_return_all_users() {
      // given
      UserDto dto1 = DtoFactory.UserDtoFactory.withUsernameAndEmail("user1", "user1@example.com");
      UserDto dto2 = DtoFactory.UserDtoFactory.withUsernameAndEmail("user2", "user2@example.com");
      UserDto dto3 = DtoFactory.UserDtoFactory.withUsernameAndEmail("user3", "user3@example.com");
      when(userService.getAllUsers()).thenReturn(List.of(dto1, dto2, dto3));

      // when
      ValidatableResponse response = requestHandler.doGet(ContentType.JSON);

      // then
      response.contentType(ContentType.JSON)
              .statusCode(HttpStatus.OK.value());

      List<UserResponse> userList = response.extract().body().jsonPath().getList(".", UserResponse.class);
      assertThat(userList).hasSize(3);
      assertThat(userList.get(0)).isEqualTo(modelMapper.map(dto1, UserResponse.class));
      assertThat(userList.get(1)).isEqualTo(modelMapper.map(dto2, UserResponse.class));
      assertThat(userList.get(2)).isEqualTo(modelMapper.map(dto3, UserResponse.class));
    }

    @Test
    @DisplayName("Should return empty response if no users exist")
    void should_return_empty_response() {
      // given
      when(userService.getAllUsers()).thenReturn(Collections.emptyList());

      // when
      ValidatableResponse response = requestHandler.doGet(ContentType.JSON);

      // then
      response.contentType(ContentType.JSON)
              .statusCode(HttpStatus.OK.value());

      List<UserResponse> userList = response.extract().body().jsonPath().getList(".", UserResponse.class);
      assertThat(userList).isEmpty();
    }

    @Test
    @DisplayName("Should use UserService to return all users")
    void should_use_user_service() {
      // given
      when(userService.getAllUsers()).thenReturn(Collections.emptyList());

      // when
      requestHandler.doGet(ContentType.JSON);

      // then
      verify(userService, times((1))).getAllUsers();
    }

  }

  @DisplayName("Get certain user tests")
  @Nested
  class GetCertainUserTest {

    @Test
    @DisplayName("Should return a certain user")
    void should_return_a_certain_user() {
      // given
      UserDto dto = DtoFactory.UserDtoFactory.withUsernameAndEmail("user1", "user1@example.com");
      when(userService.getUserByPublicId(anyString())).thenReturn(dto);

      // when
      ValidatableResponse response = requestHandler.doGet("/dummy-user-id", ContentType.JSON);

      // then
      response.contentType(ContentType.JSON)
              .statusCode(HttpStatus.OK.value());

      UserResponse user = response.extract().as(UserResponse.class);
      assertThat(user).isEqualTo(modelMapper.map(dto, UserResponse.class));
    }

    @Test
    @DisplayName("Should use UserService to return a certain user")
    void should_use_user_service() {
      // given
      final String USER_ID = "1234";
      when(userService.getUserByPublicId(USER_ID)).thenReturn(new UserDto());

      // when
      requestHandler.doGet("/" + USER_ID, ContentType.JSON);

      // then
      verify(userService, times(1)).getUserByPublicId(USER_ID);
    }

    @Test
    @DisplayName("Should return 404 if no user exist")
    void should_return_404() {
      // given
      final String USER_ID = "1234";
      when(userService.getUserByPublicId(USER_ID)).thenThrow(new ResourceNotFoundException("msg"));

      // when
      ValidatableResponse response = requestHandler.doGet("/" + USER_ID, ContentType.JSON);

      // then
      response.contentType(ContentType.JSON)
              .statusCode(HttpStatus.NOT_FOUND.value());
    }

  }

}