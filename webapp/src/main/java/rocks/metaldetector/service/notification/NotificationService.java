package rocks.metaldetector.service.notification;

public interface NotificationService {

  void notifyAllUsers();
  void notifyUser(String publicUserId);
}