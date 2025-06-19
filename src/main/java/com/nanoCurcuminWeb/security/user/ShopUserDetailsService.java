package com.nanoCurcuminWeb.security.user;

import com.nanoCurcuminWeb.model.User;
import com.nanoCurcuminWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .map(ShopUserDetails::buildUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException("Email not found: " + identifier));
        } else {
            return userRepository.findByUserName(identifier)
                    .map(ShopUserDetails::buildUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found: " + identifier));
        }
    }

}
