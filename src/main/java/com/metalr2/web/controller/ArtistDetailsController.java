package com.metalr2.web.controller;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.config.constants.ViewNames;
import com.metalr2.web.controller.discogs.ArtistSearchRestClient;
import com.metalr2.web.dto.discogs.artist.Artist;
import com.metalr2.web.dto.discogs.artist.Member;
import com.metalr2.web.dto.discogs.misc.Image;
import com.metalr2.web.dto.response.ArtistDetailsResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ArtistDetailsController {

  private final ArtistSearchRestClient artistSearchRestClient;
  private static final String DEFAULT_ARTIST_ID = "0";
  private static final String DEFAULT_ARTIST_NAME = "";

  @Autowired
  public ArtistDetailsController(ArtistSearchRestClient artistSearchRestClient) {
    this.artistSearchRestClient = artistSearchRestClient;
  }

  @GetMapping({Endpoints.Frontend.ARTIST_DETAILS})
  public ModelAndView handleSearchRequest(@RequestParam(name = "artistName", defaultValue = DEFAULT_ARTIST_NAME) String artistName,
                                          @RequestParam(name = "id", defaultValue = DEFAULT_ARTIST_ID) long artistId) {
    return createArtistDetailsModelAndView(artistName, artistId);
  }

  private ModelAndView createArtistDetailsModelAndView(String artistName, long artistId) {
    Optional<Artist> artistOptional = artistSearchRestClient.searchById(artistId);

    if (artistOptional.isEmpty()) {
      return createBadArtistIdSearchRequestModelAndView(artistName, artistId);
    }

    Artist artist                               = artistOptional.get();
    ArtistDetailsResponse artistDetailsResponse = createArtistDetailsResponse(artistName,artist);

    HashMap<String, Object> viewModel = new HashMap<>();
    viewModel.put("artistName", artistName);
    viewModel.put("artistId", artistId);
    viewModel.put("artistDetailsResponse", artistDetailsResponse);

    return new ModelAndView(ViewNames.Frontend.ARTIST_DETAILS, viewModel);
  }

  private ModelAndView createBadArtistIdSearchRequestModelAndView(String artistName, long artistId) {
    Map<String, Object> viewModel = new HashMap<>();
    viewModel.put("artistName", artistName);
    viewModel.put("artistId", artistId);
    return new ModelAndView(ViewNames.Frontend.ARTIST_DETAILS, viewModel);
  }

  private ArtistDetailsResponse createArtistDetailsResponse(String artistName, Artist artist) {
    String artistProfile      = artist.getProfile().isEmpty() ? null : artist.getProfile();
    List<String> activeMember = artist.getMembers() == null   ? null : artist.getMembers().stream().filter(Member::isActive).map(Member::getName).collect(Collectors.toList());
    List<String> formerMember = artist.getMembers() == null   ? null : artist.getMembers().stream().filter(member -> !member.isActive()).map(Member::getName).collect(Collectors.toList());
    List<String> images       = artist.getImages()  == null   ? null : artist.getImages().stream().map(Image::getResourceUrl).collect(Collectors.toList());

    return new ArtistDetailsResponse(artistProfile, activeMember, formerMember, images);
  }
}
