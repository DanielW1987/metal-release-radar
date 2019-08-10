package com.metalr2.model.exceptions;

public enum ErrorMessages {

  USER_WITH_EMAIL_ALREADY_EXISTS("This email address has already been used, please choose another one."),
  USER_WITH_USERNAME_ALREADY_EXISTS("This username has already been used, please choose another one."),
  RESOURCE_DOES_NOT_EXIST("Resource does not exist."),
  USER_WITH_ID_NOT_FOUND("User with provided id not found."),
  USER_NOT_FOUND("User with provided email or username not found."),
  TOKEN_NOT_FOUND("Token not found."), // what to do?
  EMAIL_VERIFICATION_TOKEN_EXPIRED("Email verification token is expired."),
  PASSWORD_RESET_TOKEN_EXPIRED("Password reset token is expired. Please reset the password again.");

  private final String errorMessage;
	
  ErrorMessages(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  public String toDisplayString() {
    return errorMessage;
  }

}
