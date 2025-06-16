package com.nanoCurcuminWeb.dto;

import lombok.Data;

import java.util.List;

@Data
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
