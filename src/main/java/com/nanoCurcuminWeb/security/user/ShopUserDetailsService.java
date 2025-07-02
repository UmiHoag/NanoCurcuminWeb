package com.nanoCurcuminWeb.security.user;

import com.nanoCurcuminWeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final MessageSource messageSource;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        if (identifier.contains("@")) {
            return userRepository.findByEmail(identifier)
                    .map(ShopUserDetails::buildUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("email.not.found", new Object[]{identifier}, LocaleContextHolder.getLocale())));
        } else {
            return userRepository.findByUserName(identifier)
                    .map(ShopUserDetails::buildUserDetails)
                    .orElseThrow(() -> new UsernameNotFoundException(messageSource.getMessage("user.name.not.found", new Object[]{identifier}, LocaleContextHolder.getLocale())));
        }
    }
}
