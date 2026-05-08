package com.guzem.uzaktan.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {

    private final Long userId;
    private final boolean passwordResetRequired;

    public CustomUserDetails(String username, String password,
                             boolean enabled, boolean accountNonLocked,
                             Collection<? extends GrantedAuthority> authorities,
                             Long userId, boolean passwordResetRequired) {
        super(username, password, enabled, true, true, accountNonLocked, authorities);
        this.userId = userId;
        this.passwordResetRequired = passwordResetRequired;
    }
}
