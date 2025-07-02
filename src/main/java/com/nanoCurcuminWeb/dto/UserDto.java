package com.nanoCurcuminWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String password;
    private String email;
    private String address;
    private String phoneNumber;
    private String markAsDeleted;
    private String isAuthenticated;
    private List<OrderDto> orders;
    private CartDto cart;
}
