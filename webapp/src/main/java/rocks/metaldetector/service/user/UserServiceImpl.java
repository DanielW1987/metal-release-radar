package rocks.metaldetector.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.comparator.BooleanComparator;
import rocks.metaldetector.persistence.domain.token.TokenEntity;
import rocks.metaldetector.persistence.domain.token.TokenRepository;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.persistence.domain.user.UserRepository;
import rocks.metaldetector.persistence.domain.user.UserRole;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.service.exceptions.IllegalUserException;
import rocks.metaldetector.service.exceptions.TokenExpiredException;
import rocks.metaldetector.service.exceptions.UserAlreadyExistsException;
import rocks.metaldetector.service.token.TokenService;
import rocks.metaldetector.support.JwtsSupport;
import rocks.metaldetector.support.ResourceNotFoundException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static rocks.metaldetector.persistence.domain.user.UserRole.ROLE_ADMINISTRATOR;

@Service
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TokenRepository tokenRepository;
  private final JwtsSupport jwtsSupport;
  private final UserMapper userMapper;
  private final TokenService tokenService;
  private final CurrentUserSupplier currentUserSupplier;

  @Override
  @Transactional
  public UserDto createUser(UserDto userDto) {
    return createUserEntity(userDto, UserRole.createUserRole(), false);
  }

  @Override
  @Transactional
  public UserDto createAdministrator(UserDto userDto) {
    return createUserEntity(userDto, UserRole.createAdministratorRole(), true);
  }

  private UserDto createUserEntity(UserDto userDto, Set<UserRole> roles, boolean enabled) {
    checkIfUserAlreadyExists(userDto.getUsername(), userDto.getEmail());

    // create user
    UserEntity userEntity = UserEntity.builder()
        .username(userDto.getUsername())
        .email(userDto.getEmail())
        .password(passwordEncoder.encode(userDto.getPlainPassword()))
        .userRoles(roles)
        .enabled(enabled)
        .build();

    UserEntity savedUserEntity = userRepository.save(userEntity);

    return userMapper.mapToDto(savedUserEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public UserDto getUserByPublicId(String publicId) {
    UserEntity userEntity = userRepository.findByPublicId(publicId)
                                          .orElseThrow(() -> new ResourceNotFoundException(UserErrorMessages.USER_WITH_ID_NOT_FOUND.toDisplayString()));

    return userMapper.mapToDto(userEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<UserDto> getUserByEmailOrUsername(String emailOrUsername) {
    Optional<UserEntity> userEntity = findByEmailOrUsername(emailOrUsername);
    return userEntity.map(userMapper::mapToDto);
  }

  @Override
  @Transactional
  public UserDto updateUser(String publicId, UserDto userDto) {
    UserEntity userEntity = userRepository.findByPublicId(publicId)
                                          .orElseThrow(() -> new ResourceNotFoundException(UserErrorMessages.USER_WITH_ID_NOT_FOUND.toDisplayString()));

    if (publicId.equals(currentUserSupplier.get().getPublicId())) {
      if (!userDto.isEnabled())
        throw IllegalUserException.createAdminCannotDisableHimselfException();
      if (userEntity.isAdministrator() && !UserRole.getRoleFromString(userDto.getRole()).contains(ROLE_ADMINISTRATOR))
        throw IllegalUserException.createAdminCannotDiscardHisRoleException();
    }

    userEntity.setUserRoles(UserRole.getRoleFromString(userDto.getRole()));
    userEntity.setEnabled(userDto.isEnabled());

    UserEntity updatedUserEntity = userRepository.save(userEntity);

    return userMapper.mapToDto(updatedUserEntity);
  }

  @Override
  @Transactional
  public void deleteUser(String publicId) {
    UserEntity userEntity = userRepository.findByPublicId(publicId)
                                          .orElseThrow(() -> new ResourceNotFoundException(UserErrorMessages.USER_WITH_ID_NOT_FOUND.toDisplayString()));
    userRepository.delete(userEntity);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> getAllUsers() {
    return userRepository.findAll()
            .stream()
            .map(userMapper::mapToDto)
            .sorted(Comparator.comparing(UserDto::isEnabled, BooleanComparator.TRUE_LOW).thenComparing(UserDto::getRole).thenComparing(UserDto::getUsername))
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> getAllActiveUsers() {
    return userRepository.findAll()
        .stream()
        .map(userMapper::mapToDto)
        .filter(UserDto::isEnabled)
        .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserDto> getAllUsers(int page, int limit) {
    Pageable pageable = PageRequest.of(page, limit);

    return userRepository.findAll(pageable)
            .stream()
            .map(userMapper::mapToDto)
            .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String emailOrUsername) throws UsernameNotFoundException {
    return findByEmailOrUsername(emailOrUsername).orElseThrow(() -> new UsernameNotFoundException(UserErrorMessages.USER_NOT_FOUND.toDisplayString()));
  }

  @Override
  @Transactional
  public void verifyEmailToken(String tokenString) {
    // check if token exists
    TokenEntity tokenEntity = tokenRepository.findEmailVerificationToken(tokenString)
                                             .orElseThrow(() -> new ResourceNotFoundException(UserErrorMessages.TOKEN_NOT_FOUND.toDisplayString()));

    // check if token is expired
    if (tokenEntity.isExpired()) {
      throw new TokenExpiredException();
    }

    // get claims to check signature of token
    jwtsSupport.getClaims(tokenString);

    // verify registration
    UserEntity userEntity = tokenEntity.getUser();
    userEntity.setEnabled(true);
    userRepository.save(userEntity);

    tokenRepository.delete(tokenEntity);
  }

  @Override
  @Transactional
  public void changePassword(String tokenString, String newPassword) {
    // 1. get claims to check signature of token
    jwtsSupport.getClaims(tokenString);

    // 2. get user from token if it exists
    TokenEntity tokenEntity = tokenService.getResetPasswordTokenByTokenString(tokenString)
                                          .orElseThrow(() -> new ResourceNotFoundException(UserErrorMessages.TOKEN_NOT_FOUND.toDisplayString()));

    // 3. check if token is expired
    if (tokenEntity.isExpired()) {
      throw new TokenExpiredException();
    }

    UserEntity userEntity = tokenEntity.getUser();

    // 4. set new password
    userEntity.setPassword(passwordEncoder.encode(newPassword));
    userRepository.save(userEntity);

    // 5. remove token from database
    tokenService.deleteToken(tokenEntity);
  }

  private Optional<UserEntity> findByEmailOrUsername(String emailOrUsername) {
    // try to find user by email
    Optional<UserEntity> userEntity = userRepository.findByEmail(emailOrUsername);

    // If no user was found by email, a user will be searched by username
    if (userEntity.isEmpty()) {
      userEntity = userRepository.findByUsername(emailOrUsername);
    }

    return userEntity;
  }

  /*
   * It's not allowed to use someones username or email.
   * It's also not allowed to use someones email as username and vice versa.
   */
  private void checkIfUserAlreadyExists(String username, String email) {
    if (userRepository.existsByUsername(username) || userRepository.existsByEmail(username)) {
      throw UserAlreadyExistsException.createUserWithUsernameAlreadyExistsException();
    }
    else if (userRepository.existsByEmail(email) || userRepository.existsByUsername(email)) {
      throw UserAlreadyExistsException.createUserWithEmailAlreadyExistsException();
    }
  }
}
