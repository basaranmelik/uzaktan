package com.guzem.uzaktan.security;

import com.guzem.uzaktan.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LoginEventListener {

    private final UserService userService;

    @EventListener
    public void onAuthFailure(AbstractAuthenticationFailureEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String email) {
            userService.recordLoginFailure(email);
        }
    }

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent event) {
        userService.recordLoginSuccess(event.getAuthentication().getName());
    }
}
