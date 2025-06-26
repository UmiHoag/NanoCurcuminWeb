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
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAllByMarkAsDeletedNot(DeletedStatus.DELETED.getCode()).stream()
                .map(this::convertUserToDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            log.error("Attempted to create user with invalid email");
            throw new IllegalArgumentException("Email cannot be null or empty");
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
                    user.setMarkAsDeleted(DeletedStatus.ACTIVE.getCode());
                    user.setIsAuthenticated(AuthenticationStatus.NOT_AUTHENTICATED.getCode());
                    User savedUser = userRepository.save(user);
                    log.info("Created user with email: {}", savedUser.getEmail());
                    sendVerificationEmail(savedUser);
                    return savedUser;
                }).orElseThrow(() -> new AlreadyExistsException("Email " + request.getEmail() + " already exists!"));
    }

    @Override
    public void sendVerificationEmail(User user) {
        if (user == null || user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            log.error("Cannot send verification email: user or email is null");
            throw new IllegalArgumentException("User or email cannot be null");
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        try {
            userRepository.save(user);
            log.info("Saved verification token for user: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to save verification token for user: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to save verification token", e);
        }

        String subject = "Verify your Email Address";
        String verificationUrl = "http://localhost:9191/api/auth/verify?token=" + token;
        String message = """
            <html>
            <body>
                <p>Click the link below to verify your email address:</p>
                <p><a href="%s">Verify Email</a></p>
            </body>
            </html>
            """.formatted(verificationUrl);

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mimeMessage);
            log.info("Verification email sent to: {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send verification email to: {}", user.getEmail(), e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    @Override
    public void verifyEmail(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.error("Invalid verification token: null or empty");
            throw new IllegalArgumentException("Verification token cannot be null or empty");
        }
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid verification token: " + token));
        user.setIsAuthenticated(AuthenticationStatus.AUTHENTICATED.getCode());
        user.setVerificationToken(null);
        userRepository.save(user);
        log.info("Email verified for user: {}", user.getEmail());
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        return findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            User updatedUser = userRepository.save(existingUser);
            log.info("Updated user with ID: {}", userId);
            return updatedUser;
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
    }

    @Override
    public void deleteUser(Long userId) {
        User user = findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setMarkAsDeleted(DeletedStatus.DELETED.getCode());
        userRepository.save(user);
        log.info("Soft deleted user with ID: {}", userId);
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        String email = authentication.getName();
        return userRepository.findByEmailAndMarkAsDeletedNot(email, DeletedStatus.DELETED.getCode())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Optional<User> findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        return userRepository.findByIdAndMarkAsDeletedNot(userId, DeletedStatus.DELETED.getCode());
    }
}