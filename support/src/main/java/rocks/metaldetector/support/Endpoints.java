package rocks.metaldetector.support;

import java.util.List;
import java.util.stream.Stream;

public class Endpoints {

  public static final String ERROR = "/error";

  public static class Guest {
    // General
    public static final String        INDEX                  = "/index";
    public static final String        EMPTY_INDEX            = "";
    public static final String        SLASH_INDEX            = "/";
    public static final String        LOGOUT                 = "/logout";
    public static final String        IMPRINT                = "/guest/imprint";
    public static final String        PRIVACY_POLICY         = "/guest/privacy-policy";
    public static final List<String>  ALL_GUEST_INDEX_PAGES  = List.of(INDEX, EMPTY_INDEX, SLASH_INDEX, IMPRINT, PRIVACY_POLICY);

    // Authentication
    public static final String       LOGIN                     = "/login";
    public static final String       REGISTER                  = "/register";
    public static final String       REGISTRATION_VERIFICATION = "/registration-verification";
    public static final String       RESEND_VERIFICATION_TOKEN = "/resend-verification-token";
    public static final String       FORGOT_PASSWORD           = "/forgot-password";
    public static final String       RESET_PASSWORD            = "/reset-password";
    public static final List<String> ALL_AUTH_PAGES            = List.of(LOGIN, REGISTER, REGISTRATION_VERIFICATION,
                                                                         RESEND_VERIFICATION_TOKEN, FORGOT_PASSWORD, RESET_PASSWORD);
  }

  public static class Frontend {
    public static final String HOME                        = "/home";
    public static final String ARTISTS                     = "/artists/search";
    public static final String SETTINGS                    = "/settings";
    public static final String PROFILE                     = "/profile";
    public static final String RELEASES                    = "/releases";
    public static final String MY_ARTISTS                  = "/my-artists";
    public static final String BLOG                        = "/blog";
    public static final String BLOG_POST_WE_ARE_ONLINE     = "/blog/we-are-online";
    public static final String BLOG_POST_TOP_RELEASES_2020 = "/blog/top-6-releases-2020";
    public static final String BLOG_POST_FUTURE_PLANS      = "/blog/future-plans";
    public static final String IMPRINT                     = "/imprint";
    public static final String PRIVACY_POLICY              = "/privacy-policy";
    public static final String STATUS                      = "/status";
    public static final String ACCOUNT_DETAILS             = "/settings/account-details";
    public static final String SPOTIFY_SYNCHRONIZATION     = "/settings/spotify-synchronization";
    public static final String NOTIFICATION_SETTINGS       = "/settings/notification-settings";
    public static final String TEST                        = "/only-for-testing";
    public static final String SPOTIFY_CALLBACK            = "/spotify-callback";
    public static final List<String> ALL_FRONTEND_PAGES    = List.of(HOME, ARTISTS, SETTINGS, PROFILE, RELEASES, MY_ARTISTS,
                                                                     BLOG, BLOG_POST_WE_ARE_ONLINE, BLOG_POST_FUTURE_PLANS, BLOG_POST_TOP_RELEASES_2020,
                                                                     IMPRINT, PRIVACY_POLICY, STATUS, TEST, SPOTIFY_CALLBACK,ACCOUNT_DETAILS,
                                                                     SPOTIFY_SYNCHRONIZATION,NOTIFICATION_SETTINGS);
  }

  public static class Rest {
    public static final String HOME                           = "/rest/v1/home";
    public static final String ARTISTS                        = "/rest/v1/artists";
    public static final String MY_ARTISTS                     = "/rest/v1/my-artists";
    public static final String SPOTIFY_AUTHORIZATION          = "/rest/v1/spotify/auth";
    public static final String SPOTIFY_ARTIST_SYNCHRONIZATION = "/rest/v1/spotify/synchronize";
    public static final String SPOTIFY_SAVED_ARTISTS          = "/rest/v1/spotify/saved-artists";
    public static final String ALL_RELEASES                   = "/rest/v1/releases/all";
    public static final String MY_RELEASES                    = "/rest/v1/releases/my";
    public static final String RELEASES                       = "/rest/v1/releases";
    public static final String TOP_RELEASES                   = "/rest/v1/releases/top";
    public static final String IMPORT_JOB                     = "/rest/v1/releases/import";
    public static final String COVER_JOB                      = "/rest/v1/releases/cover-reload";
    public static final String CSP_VIOLATION_REPORT           = "/rest/v1/csp-violation-report";
    public static final String NOTIFICATION_CONFIG            = "/rest/v1/notification-config";
    public static final String TELEGRAM_CONFIG                = "/rest/v1/telegram-config";
    public static final String SEARCH                         = "/search";
    public static final String FOLLOW                         = "/follow";
    public static final String UNFOLLOW                       = "/unfollow";

    public static final String USERS = "/rest/v1/users";

    public static final String CURRENT_USER       = "/rest/v1/me";
    public static final String CURRENT_USER_EMAIL = "/rest/v1/me/email";
    public static final String CURRENT_USER_PASSWORD = "/rest/v1/me/password";

    public static final String NOTIFICATION_ON_FREQUENCY = "/rest/v1/notify/frequency";
    public static final String NOTIFICATION_ON_RELEASE_DATE = "/rest/v1/notify/release-date";
    public static final String NOTIFICATION_ON_ANNOUNCEMENT_DATE = "/rest/v1/notify/announcement-date";
    public static final String NOTIFICATION_TELEGRAM = "/rest/v1/notify/telegram";

    public static final String TEST = "/rest/v1/only-for-testing";
  }

  public static class AdminArea {
    public static final String INDEX          = "/admin";
    public static final String IMPORT         = "/admin/import";
    public static final String ANALYTICS      = "/admin/analytics";
    public static final String SETTINGS       = "/admin/settings";
    public static final String RELEASES       = "/admin/releases";
    public static final String USERS          = "/admin/users";
    public static final String PROFILE        = "/admin/profile";
    public static final String NOTIFICATIONS  = "/admin/notifications";
  }

  public static class AntPattern {
    public static final String   ADMIN                     = "/admin/**";
    public static final String   REST_ENDPOINTS            = "/rest/**";
    public static final String   ACTUATOR_ENDPOINTS        = "/actuator/**";
    public static final String[] LOGIN                     = {"/login", "/login/"};
           static final String[] INDEX                     = {"/", "/index", "/index/"};
           static final String[] REGISTER                  = {"/register", "/register/"};
           static final String[] REGISTRATION_VERIFICATION = {"/registration-verification", "/registration-verification/"};
           static final String[] RESEND_VERIFICATION_TOKEN = {"/resend-verification-token", "/resend-verification-token/"};
           static final String[] FORGOT_PASSWORD           = {"/forgot-password", "/forgot-password/"};
           static final String[] RESET_PASSWORD            = {"/reset-password", "/reset-password/"};
           static final String[] GUEST_IMPRINT             = {"/guest/imprint", "/guest/imprint/"};
           static final String[] GUEST_PRIVACY_POLICY      = {"/guest/privacy-policy", "/guest/privacy-policy/"};
    public static final String[] RESOURCES                 = {"/resources/**", "/css/**", "/js/**", "/images/**", "/webjars/**"};

    public static final String[] GUEST_PAGES = Stream.of(INDEX, LOGIN, REGISTER, REGISTRATION_VERIFICATION,
            RESEND_VERIFICATION_TOKEN, FORGOT_PASSWORD, RESET_PASSWORD, GUEST_IMPRINT, GUEST_PRIVACY_POLICY).flatMap(Stream::of).toArray(String[]::new);
  }
}
