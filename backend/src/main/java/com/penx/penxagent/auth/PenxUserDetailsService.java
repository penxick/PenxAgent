
package com.penx.penxagent.auth;

import com.penx.penxagent.user.User;
import com.penx.penxagent.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class PenxUserDetailsService implements UserDetailsService {

    private final UserRepository users;

    public PenxUserDetailsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String login) {
        String normalized = login.trim();

        User user = normalized.contains("@")
                ? users.findByEmail(
                        normalized.toLowerCase(Locale.ROOT))
                    .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid credentials"))
                : users.findByUsername(normalized)
                    .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Invalid credentials"));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getId().toString())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}
