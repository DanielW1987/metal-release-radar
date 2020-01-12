package com.metalr2.model.artist;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowedArtistsRepository extends JpaRepository<FollowedArtistEntity, Long> {

  List<FollowedArtistEntity> findAllByPublicUserId(String publicUserId);

  boolean existsByPublicUserIdAndDiscogsId(String publicUserId, long discogsId);

  Optional<FollowedArtistEntity> findByPublicUserIdAndDiscogsId(String publicUserId, long discogsId);

  List<FollowedArtistEntity> findAllByPublicUserIdAndDiscogsIdIn(String publicUserId, long... discgogsIds);

  List<FollowedArtistEntity> findAllByPublicUserId(String publicUserId, Pageable pageable);

}
