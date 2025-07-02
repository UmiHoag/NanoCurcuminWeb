package com.nanoCurcuminWeb.service.user;

import com.nanoCurcuminWeb.dto.UserDto;
import com.nanoCurcuminWeb.enums.AuthenticationStatus;
import com.nanoCurcuminWeb.enums.DeletedStatus;
import com.nanoCurcuminWeb.exceptions.AlreadyExistsException;
import com.nanoCurcuminWeb.exceptions.ResourceNotFoundException;
import com.nanoCurcuminWeb.model.User;
import com.nanoCurcuminWeb.repository.UserRepository;
import com.nanoCurcuminWeb.request.CreateUserRequest;
import com.nanoCurcuminWeb.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final MessageSource messageSource;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findByMarkAsDeletedFalse().stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        messageSource.getMessage(
                                "user.not.found.with.id", new Object[]{userId}, LocaleContextHolder.getLocale()
                        )
                ));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            "email.cannot.be.null.or.empty", null, LocaleContextHolder.getLocale()));
        }
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setUserName(request.getUserName());
                    user.setAddress(request.getAddress());
                    user.setPhoneNumber(request.getPhoneNumber());
                    user.setMarkAsDeleted(false);
                    user.setIsAuthenticated(false);
                    String token = UUID.randomUUID().toString();
                    user.setVerificationToken(token);
                    User savedUser = userRepository.save(user);
                    sendVerificationEmail(savedUser);
                    return savedUser;
                }).orElseThrow(() -> new AlreadyExistsException(messageSource.getMessage("email.already.exists", new Object[]{request.getEmail()}, LocaleContextHolder.getLocale())));
    }

    @Override
    public void sendVerificationEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("user.email.cant.be.null", null, LocaleContextHolder.getLocale()));
        }

        String subject = messageSource.getMessage("verification.mail.subject", null, LocaleContextHolder.getLocale());
        String greeting = messageSource.getMessage("verification.mail.greeting", new Object[]{user.getFirstName()}, LocaleContextHolder.getLocale());
        String content = messageSource.getMessage("verification.mail.content", null, LocaleContextHolder.getLocale());
        String subcontent = messageSource.getMessage("verification.mail.subcontent", null, LocaleContextHolder.getLocale());
        String button = messageSource.getMessage("verification.mail.button", null, LocaleContextHolder.getLocale());

        String message = String.format("""
                <html>
                <body>
                    <p style="color: #000000;">%s</p>
                    <p style="color: #000000;">%s</p>
                    <p style="color: #000000;">%s</p>
                    <p style="text-align: center;">
                        <a href="http://localhost:8000/verify" style="background-color: #f97316; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;">
                            %s
                        </a>
                    </p>
                </body>
                </html>
                """,
                greeting,
                content,
                subcontent,
                button
        );

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException(messageSource.getMessage("failed.to.send.verification.email", new Object[]{e}, LocaleContextHolder.getLocale()));
        }
    }

    @Override
    public void verifyEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new IllegalArgumentException(messageSource.getMessage("verification.token.cannot.be.null", null, LocaleContextHolder.getLocale()));
        }
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("invalid.verification.token", null, LocaleContextHolder.getLocale())));
        user.setIsAuthenticated(true);
        user.setVerificationToken(null);
        userRepository.save(user);
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        return findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName()); 
            existingUser.setLastName(request.getLastName());
            User updatedUser = userRepository.save(existingUser);
            return updatedUser;
        }).orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("user.not.found.with.id", new Object[]{userId}, LocaleContextHolder.getLocale())));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("user.not.found.with.id", new Object[]{userId}, LocaleContextHolder.getLocale())));
        user.setMarkAsDeleted(true);
        userRepository.save(user);
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException(messageSource.getMessage("no.authenticated.user.found", null, LocaleContextHolder.getLocale()));
        }
        String email = authentication.getName();
        return userRepository.findByEmailAndMarkAsDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException(messageSource.getMessage("user.not.found.with.email", new Object[]{email}, LocaleContextHolder.getLocale())));
    }

    private Optional<User> findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(messageSource.getMessage("user.id.cannot.be.null", null, LocaleContextHolder.getLocale()));
        }
        return userRepository.findByIdAndMarkAsDeletedFalse(userId);
    }
}