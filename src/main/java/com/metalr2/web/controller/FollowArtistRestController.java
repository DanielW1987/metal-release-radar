package com.metalr2.web.controller;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.model.exceptions.ErrorMessages;
import com.metalr2.model.exceptions.ValidationException;
import com.metalr2.service.followArtist.FollowArtistService;
import com.metalr2.web.dto.FollowArtistDto;
import com.metalr2.web.dto.request.FollowArtistRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping(Endpoints.Rest.FOLLOW_ARTISTS_V1)
public class FollowArtistRestController {

  private final FollowArtistService followArtistService;
  private final ModelMapper mapper;

  @Autowired
  public FollowArtistRestController(FollowArtistService followArtistService){
    this.followArtistService = followArtistService;
    this.mapper = new ModelMapper();
  }

  @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Void> followArtist(@Valid @RequestBody FollowArtistRequest followArtistRequest, BindingResult bindingResult) {
    validateRequest(bindingResult);

    FollowArtistDto followArtistDto = mapper.map(followArtistRequest, FollowArtistDto.class);
    followArtistService.followArtist(followArtistDto);

    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @DeleteMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Void> unfollowArtist(@Valid @RequestBody FollowArtistRequest followArtistRequest, BindingResult bindingResult) {
    validateRequest(bindingResult);

    FollowArtistDto followArtistDto = mapper.map(followArtistRequest, FollowArtistDto.class);
    boolean success = followArtistService.unfollowArtist(followArtistDto);

    return success ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
  }

  private void validateRequest(BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      throw new ValidationException(ErrorMessages.VALIDATION_ERROR.toDisplayString(), bindingResult.getFieldErrors());
    }
  }
}
