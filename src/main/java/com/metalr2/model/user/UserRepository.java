package com.metalr2.model.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
	
  Optional<UserEntity> findByEmail(String email);

  Optional<UserEntity> findByUsername(String username);

  Optional<UserEntity> findByUserId(String userId);

  boolean existsByEmail(String email);

  boolean existsByUsername(String username);
	
}
