package rocks.metaldetector.service.notification;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rocks.metaldetector.butler.facade.ReleaseService;
import rocks.metaldetector.butler.facade.dto.ReleaseDto;
import rocks.metaldetector.persistence.domain.notification.NotificationConfigEntity;
import rocks.metaldetector.persistence.domain.notification.NotificationConfigRepository;
import rocks.metaldetector.persistence.domain.user.UserEntity;
import rocks.metaldetector.security.CurrentUserSupplier;
import rocks.metaldetector.service.artist.ArtistDto;
import rocks.metaldetector.service.artist.FollowArtistService;
import rocks.metaldetector.service.email.EmailService;
import rocks.metaldetector.service.email.ReleasesEmail;
import rocks.metaldetector.service.email.TodaysAnnouncementsEmail;
import rocks.metaldetector.service.email.TodaysReleasesEmail;
import rocks.metaldetector.support.TimeRange;
import rocks.metaldetector.support.exceptions.ResourceNotFoundException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.WEEKS;

@Service
@AllArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  static final List<Integer> SUPPORTED_FREQUENCIES = List.of(2, 4);

  private final ReleaseService releaseService;
  private final EmailService emailService;
  private final FollowArtistService followArtistService;
  private final NotificationConfigRepository notificationConfigRepository;
  private final NotificationConfigTransformer notificationConfigTransformer;
  private final CurrentUserSupplier currentUserSupplier;

  @Override
//  @Scheduled(cron = "0 0 4 * * SUN")
  @Transactional
  public void notifyOnFrequency() {
    List<ReleaseContainer> releaseContainer = createReleaseContainer();

    notificationConfigRepository.findAll().stream()
        .filter(config -> config.getUser().isEnabled() &&
                          config.getNotify())
        .forEach(notificationConfig -> frequencyNotification(notificationConfig, getReleaseContainerForConfig(releaseContainer, notificationConfig)));
  }

  @Override
//  @Scheduled(cron = "0 0 7 * * *")
  @Transactional(readOnly = true)
  public void notifyOnReleaseDate() {
    var now = LocalDate.now();
    List<ReleaseDto> todaysReleases = releaseService.findAllReleases(Collections.emptyList(), new TimeRange(now, now));

    notificationConfigRepository.findAll().stream()
        .filter(config -> config.getUser().isEnabled() &&
                          config.getNotify() &&
                          config.getNotificationAtReleaseDate())
        .forEach(notificationConfig -> releaseDateNotification(notificationConfig, todaysReleases));
  }

  @Override
//  @Scheduled(cron = "0 0 7 * * *")
  @Transactional(readOnly = true)
  public void notifyOnAnnouncementDate() {
    var now = LocalDate.now();
    List<ReleaseDto> todaysAnnouncedReleases = releaseService.findAllReleases(Collections.emptyList(), new TimeRange(now, null)).stream()
        .filter(release -> release.getAnnouncementDate().isEqual(now))
        .collect(Collectors.toList());

    notificationConfigRepository.findAll().stream()
        .filter(config -> config.getUser().isEnabled() &&
                          config.getNotify() &&
                          config.getNotificationAtAnnouncementDate())
        .forEach(notificationConfig -> announcementDateNotification(notificationConfig, todaysAnnouncedReleases));
  }

  @Override
  @Transactional(readOnly = true)
  public NotificationConfigDto getCurrentUserNotificationConfig() {
    UserEntity currentUser = currentUserSupplier.get();
    NotificationConfigEntity notificationConfigEntity = notificationConfigRepository.findByUserId(currentUser.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Notification config for user '" + currentUser.getPublicId() + "' not found"));
    return notificationConfigTransformer.transform(notificationConfigEntity);
  }

  @Override
  @Transactional
  public void updateCurrentUserNotificationConfig(NotificationConfigDto notificationConfigDto) {
    UserEntity currentUser = currentUserSupplier.get();
    NotificationConfigEntity notificationConfigEntity = notificationConfigRepository.findByUserId(currentUser.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Notification config for user '" + currentUser.getPublicId() + "' not found"));

    notificationConfigEntity.setFrequencyInWeeks(notificationConfigDto.getFrequencyInWeeks());
    notificationConfigEntity.setNotificationAtAnnouncementDate(notificationConfigDto.isNotificationAtAnnouncementDate());
    notificationConfigEntity.setNotificationAtReleaseDate(notificationConfigDto.isNotificationAtReleaseDate());
    notificationConfigEntity.setNotify(notificationConfigDto.isNotify());

    notificationConfigRepository.save(notificationConfigEntity);
  }

  private void frequencyNotification(NotificationConfigEntity notificationConfigEntity, ReleaseContainer releaseContainer) {
    var now = LocalDate.now();
    boolean shouldNotify = notificationConfigEntity.getLastNotificationDate() == null ||
                           WEEKS.between(notificationConfigEntity.getLastNotificationDate(), now) >= notificationConfigEntity.getFrequencyInWeeks();

    if (shouldNotify) {
      UserEntity user = notificationConfigEntity.getUser();
      List<String> followedArtistsNames = followArtistService.getFollowedArtistsOfUser(user.getPublicId()).stream()
          .map(ArtistDto::getArtistName).collect(Collectors.toList());

      if (!followedArtistsNames.isEmpty()) {
        List<ReleaseDto> upcomingReleases = releaseContainer.upcomingReleases.stream().filter(release -> followedArtistsNames.contains(release.getArtist())).collect(Collectors.toList());
        List<ReleaseDto> recentReleases = releaseContainer.recentReleases.stream().filter(release -> followedArtistsNames.contains(release.getArtist())).collect(Collectors.toList());

        if (!(upcomingReleases.isEmpty() && recentReleases.isEmpty())) {
          emailService.sendEmail(new ReleasesEmail(user.getEmail(), user.getUsername(), upcomingReleases, recentReleases));

          notificationConfigEntity.setLastNotificationDate(now);
          notificationConfigRepository.save(notificationConfigEntity);
        }
      }
    }
  }

  private void releaseDateNotification(NotificationConfigEntity notificationConfigEntity, List<ReleaseDto> releases) {
    UserEntity user = notificationConfigEntity.getUser();
    List<String> followedArtistsNames = followArtistService.getFollowedArtistsOfUser(user.getPublicId()).stream()
        .map(ArtistDto::getArtistName).collect(Collectors.toList());

    if (!followedArtistsNames.isEmpty()) {
      List<ReleaseDto> todaysReleases = releases.stream().filter(release -> followedArtistsNames.contains(release.getArtist())).collect(Collectors.toList());

      if (!todaysReleases.isEmpty()) {
        emailService.sendEmail(new TodaysReleasesEmail(user.getEmail(), user.getUsername(), todaysReleases));
      }
    }
  }

  private void announcementDateNotification(NotificationConfigEntity notificationConfigEntity, List<ReleaseDto> releases) {
    UserEntity user = notificationConfigEntity.getUser();
    List<String> followedArtistsNames = followArtistService.getFollowedArtistsOfUser(user.getPublicId()).stream()
        .map(ArtistDto::getArtistName).collect(Collectors.toList());

    if (!followedArtistsNames.isEmpty()) {
      List<ReleaseDto> todaysAnnouncements = releases.stream().filter(release -> followedArtistsNames.contains(release.getArtist())).collect(Collectors.toList());

      if (!todaysAnnouncements.isEmpty()) {
        emailService.sendEmail(new TodaysAnnouncementsEmail(user.getEmail(), user.getUsername(), todaysAnnouncements));
      }
    }
  }

  private List<ReleaseContainer> createReleaseContainer() {
    var now = LocalDate.now();
    List<ReleaseContainer> releaseContainers = new ArrayList<>();

    for (int frequency : SUPPORTED_FREQUENCIES) {
      List<ReleaseDto> upcomingReleases = releaseService.findAllReleases(Collections.emptyList(), new TimeRange(now, now.plusWeeks(frequency)));
      List<ReleaseDto> recentReleases = releaseService.findAllReleases(Collections.emptyList(), new TimeRange(now.minusWeeks(frequency), now.minusDays(1)));
      releaseContainers.add(new ReleaseContainer(frequency, upcomingReleases, recentReleases));
    }

    return releaseContainers;
  }

  private ReleaseContainer getReleaseContainerForConfig(List<ReleaseContainer> releaseContainer, NotificationConfigEntity notificationConfig) {
    return releaseContainer.stream()
        .filter(container -> container.numberOfWeeks == notificationConfig.getFrequencyInWeeks())
        .collect(Collectors.toList())
        .get(0);
  }

  @AllArgsConstructor
  private static class ReleaseContainer {

    private final int numberOfWeeks;
    private final List<ReleaseDto> upcomingReleases;
    private final List<ReleaseDto> recentReleases;
  }
}
