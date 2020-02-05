package com.metalr2.service.user;

import com.metalr2.model.user.UserEntity;
import com.metalr2.web.dto.UserDto;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

  UserDto createUser(UserDto userDto);

  UserDto createAdministrator(UserDto userDto);

  UserDto getUserByPublicId(String publicId);

  Optional<UserDto> getUserByEmailOrUsername(String emailOrUsername);

  UserDto updateUser(String publicId, UserDto userDto);

  void deleteUser(String publicId);

  List<UserDto> getAllUsers();

  List<UserDto> getAllUsers(int page, int limit);

  void verifyEmailToken(String tokenString);

  void changePassword(UserEntity userEntity, String newPassword);
	
}
