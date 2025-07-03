package com.nanoCurcuminWeb.request;

import lombok.Data;

@Data
public class UserUpdateRequest {
    private String firstName;
    private String lastName;
    private String userName;
    private String address;
    private Boolean isAuthenticated;
    private String phoneNumber;
}
