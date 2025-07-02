package com.nanoCurcuminWeb.service.user;

import com.nanoCurcuminWeb.dto.UserDto;
import com.nanoCurcuminWeb.model.User;
import com.nanoCurcuminWeb.request.CreateUserRequest;
import com.nanoCurcuminWeb.request.UserUpdateRequest;

import java.util.List;

public interface IUserService {

    List<UserDto> getAllUsers();

    User getUserById(Long userId);

    User createUser(CreateUserRequest request);

    User updateUser(UserUpdateRequest request, Long userId);

    void deleteUser(Long userId);

    void sendVerificationEmail(User user);

    void verifyEmail(String token);

    UserDto convertUserToDto(User user);

    User getAuthenticatedUser();
}
