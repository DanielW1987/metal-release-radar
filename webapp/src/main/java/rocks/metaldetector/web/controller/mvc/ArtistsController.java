package rocks.metaldetector.web.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import rocks.metaldetector.config.constants.Endpoints;
import rocks.metaldetector.config.constants.ViewNames;

@Controller
@RequestMapping(Endpoints.Frontend.ARTISTS)
public class ArtistsController {

  @GetMapping
  public ModelAndView showSearch() {
    return new ModelAndView(ViewNames.Frontend.SEARCH);
  }

}