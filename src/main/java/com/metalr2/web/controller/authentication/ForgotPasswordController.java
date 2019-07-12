package com.metalr2.web.controller.authentication;

import com.metalr2.config.constants.Endpoints;
import com.metalr2.config.constants.MessageKeys;
import com.metalr2.config.constants.ViewNames;
import com.metalr2.security.password.OnResetPasswordRequestCompleteEvent;
import com.metalr2.service.user.UserService;
import com.metalr2.web.dto.UserDto;
import com.metalr2.web.dto.request.ForgotPasswordRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping(Endpoints.FORGOT_PASSWORD)
public class ForgotPasswordController {

  private static final String FORM_DTO = "forgotPasswordRequest";

  private final ApplicationEventPublisher eventPublisher;
  private final UserService userService;
  private final MessageSource             messages;

  @Autowired
  public ForgotPasswordController(UserService userService, ApplicationEventPublisher eventPublisher,
                                  @Qualifier("messageSource") MessageSource messages) {
    this.userService    = userService;
    this.eventPublisher = eventPublisher;
    this.messages       = messages;
  }

  @ModelAttribute(FORM_DTO)
  private ForgotPasswordRequest forgotPasswordRequest() {
    return new ForgotPasswordRequest();
  }

  @GetMapping
  public ModelAndView showForgotPasswordForm() {
    return new ModelAndView(ViewNames.FORGOT_PASSWORD);
  }

  @PostMapping
  public ModelAndView requestPasswordReset(@Valid @ModelAttribute ForgotPasswordRequest forgotPasswordRequest, BindingResult bindingResult, WebRequest request) {
    Optional<UserDto> userDto = userService.getUserByEmail(forgotPasswordRequest.getEmail());

    if (! userDto.isPresent()) {
      bindingResult.rejectValue("email", "userDoesNotExist", messages.getMessage(MessageKeys.ForgotPassword.USER_DOES_NOT_EXIST, null, request.getLocale()));
      return new ModelAndView(ViewNames.FORGOT_PASSWORD);
    }

    eventPublisher.publishEvent(new OnResetPasswordRequestCompleteEvent(this, userDto.get()));

    Map<String, Object> viewModel = new HashMap<>();
    viewModel.put("successMessage", messages.getMessage(MessageKeys.ForgotPassword.SUCCESS, null, request.getLocale()));
    viewModel.put("forgotPasswordRequest", new ForgotPasswordRequest()); // to clear the form

    return new ModelAndView(ViewNames.FORGOT_PASSWORD, viewModel);
  }

}
