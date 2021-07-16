package rocks.metaldetector.web.controller.rest;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import rocks.metaldetector.service.summary.SummaryService;
import rocks.metaldetector.testutil.BaseWebMvcTestWithSecurity;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static rocks.metaldetector.support.Endpoints.Rest.HOME;
import static rocks.metaldetector.support.Endpoints.Rest.TOP_RELEASES;

@WebMvcTest(controllers = SummaryRestController.class)
class SummaryRestControllerIT extends BaseWebMvcTestWithSecurity {

  @MockBean
  private SummaryService summaryService;

  @Nested
  @DisplayName("Administrator is allowed to send requests to all endpoints")
  class AdministratorRoleTest {

    @Test
    @DisplayName("Administrator is allowed to GET on endpoint " + HOME + "'")
    @WithMockUser(roles = "ADMINISTRATOR")
    void admin_is_allowed_to_get_summary() throws Exception {
      mockMvc.perform(get(HOME))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Administrator is allowed to GET on endpoint " + TOP_RELEASES + "'")
    @WithMockUser(roles = "ADMINISTRATOR")
    void admin_is_allowed_to_get_top_releases() throws Exception {
      mockMvc.perform(get(TOP_RELEASES)
                          .param("maxReleases", "10")
                          .param("minFollowers", "1"))
          .andExpect(status().isOk());
    }
  }

  @Nested
  @DisplayName("User is not allowed to send requests to all endpoints")
  class UserRoleTest {

    @Test
    @DisplayName("User is allowed to GET on endpoint " + HOME + "'")
    @WithMockUser(roles = "USER")
    void user_is_allowed_to_get_summary() throws Exception {
      mockMvc.perform(get(HOME))
          .andExpect(status().isOk());
    }

    @Test
    @DisplayName("User is not allowed to GET on endpoint " + TOP_RELEASES + "'")
    @WithMockUser(roles = "USER")
    void user_is_not_allowed_to_get_top_releases() throws Exception {
      mockMvc.perform(get(TOP_RELEASES)
                          .param("maxReleases", "10")
                          .param("minFollowers", "1"))
          .andExpect(status().isForbidden());
    }
  }
}
