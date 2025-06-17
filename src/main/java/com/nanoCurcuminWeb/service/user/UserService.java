package com.nanoCurcuminWeb.service.user;

import com.nanoCurcuminWeb.dto.UserDto;
import com.nanoCurcuminWeb.enums.DeletedStatus;
import com.nanoCurcuminWeb.exceptions.AlreadyExistsException;
import com.nanoCurcuminWeb.exceptions.ResourceNotFoundException;
import com.nanoCurcuminWeb.model.User;
import com.nanoCurcuminWeb.repository.UserRepository;
import com.nanoCurcuminWeb.request.CreateUserRequest;
import com.nanoCurcuminWeb.request.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found!"));
    }

    @Override
    public User createUser(CreateUserRequest request) {
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(request.getEmail()))
                .map(req -> {
                    User user = new User();
                    user.setEmail(request.getEmail());
                    user.setPassword(passwordEncoder.encode(request.getPassword()));
                    user.setFirstName(request.getFirstName());
                    user.setLastName(request.getLastName());
                    user.setAddress(request.getAddress());
                    user.setPhoneNumber(request.getPhoneNumber());
                    return userRepository.save(user);
                }).orElseThrow(() -> new AlreadyExistsException("Oops!" + request.getEmail() + " already exists!"));
    }

    @Override
    public User updateUser(UserUpdateRequest request, Long userId) {
        return userRepository.findById(userId).map(existingUser -> {
            existingUser.setFirstName(request.getFirstName());
            existingUser.setLastName(request.getLastName());
            return userRepository.save(existingUser);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found!"));

    }

    @Override
    public void deleteUser(Long userId) {
        User deletedUser = findById(userId);
        throw new ResourceNotFoundException("User not found!");
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    // Returns user with authenticated roles (Admin role)
    @Override
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email);
    }

    private User findById(Long userId) {
        User entity = userRepository.findByIdAndMarkAsDeletedNot(userId, DeletedStatus.DELETED.getCode());
        if (entity == null) {
            throw new ResourceNotFoundException("User not found!");
        }
        return entity;
    }

    private UserDto convertToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }

//    private UserDto convertToDto(User user) {
//        UserDto dto = new UserDto();
//        dto.setId(user.getId());
//        dto.setFirstName(user.getFirstName());
//        dto.setLastName(user.getLastName());
//        dto.setEmail(user.getEmail());
//        dto.setPassword(null); // Mask or skip password
//        dto.setAddress(user.getAddress());
//        dto.setPhoneNumber(user.getPhoneNumber());
//        dto.setMarkAsDeleted(user.getMarkAsDeleted());
//        dto.setIsAuthenticated(user.getIsAuthenticated());
//
//        // Optional: Convert cart
//        if (user.getCart() != null) {
//            dto.setCart(new CartDto(/* manually map fields */));
//        }
//
//        // Optional: Convert orders
//        if (user.getOrders() != null) {
//            List<OrderDto> orders = user.getOrders().stream()
//                    .map(order -> new OrderDto(/* map fields */))
//                    .collect(Collectors.toList());
//            dto.setOrders(orders);
//        }
//
//        return dto;
//    }
}
