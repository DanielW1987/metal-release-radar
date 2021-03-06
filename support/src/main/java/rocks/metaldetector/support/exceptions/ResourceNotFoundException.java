package rocks.metaldetector.support.exceptions;

public class ResourceNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public ResourceNotFoundException(String detailedMessage) {
    super(detailedMessage);
  }
}
