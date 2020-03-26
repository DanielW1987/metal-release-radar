package rocks.metaldetector.service.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import rocks.metaldetector.support.ExternalServiceException;
import rocks.metaldetector.support.ResourceNotFoundException;
import rocks.metaldetector.web.api.response.ErrorResponse;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This class handles all Exception that can occur while using the REST API of this application.
 */
@ControllerAdvice
@Slf4j
public class AppExceptionsHandler {

  @ExceptionHandler(value = MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException exception, WebRequest webRequest) {
    log.warn(webRequest.getContextPath() + ": " + exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(value = UserAlreadyExistsException.class)
  public ResponseEntity<ErrorResponse> handleAlreadyExistsException(UserAlreadyExistsException exception, WebRequest webRequest) {
    log.warn(webRequest.getContextPath() + ": " + exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.CONFLICT);
  }

  @ExceptionHandler(value = {ResourceNotFoundException.class})
  public ResponseEntity<ErrorResponse> handleNotFoundException(RuntimeException exception, WebRequest webRequest) {
    log.warn(webRequest.getContextPath() + ": " + exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(value = {AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(RuntimeException exception, WebRequest webRequest) {
    log.warn(webRequest.getContextPath() + ": " + exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(value = {ExternalServiceException.class})
  public ResponseEntity<ErrorResponse> handleExternalServiceException(RuntimeException exception, WebRequest webRequest) {
    log.error(webRequest.getContextPath() + ": " + exception.getMessage());
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.SERVICE_UNAVAILABLE);
  }

  @ExceptionHandler(value = {Exception.class})
  public ResponseEntity<ErrorResponse> handleAllOtherExceptions(Exception exception, WebRequest webRequest) {
    log.error(webRequest.getContextPath() + ": " + exception.getMessage(), exception);
    return new ResponseEntity<>(createErrorResponse(exception), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ErrorResponse createErrorResponse(Throwable exception) {
    return new ErrorResponse(exception.getMessage());
  }

  private ErrorResponse createErrorResponse(MethodArgumentNotValidException exception) {
    List<String> fieldErrors = exception.getBindingResult().getAllErrors()
        .stream()
        .map(this::transformFieldError)
        .sorted()
        .collect(Collectors.toList());

    return new ErrorResponse(fieldErrors);
  }

  private String transformFieldError(ObjectError error) {
    if (error instanceof FieldError) {
      return "Error regarding field '" +
             ((FieldError) error).getField() +
             "': " +
             error.getDefaultMessage() +
             ".";
    }
    else {
      return error.getDefaultMessage();
    }
  }
}
