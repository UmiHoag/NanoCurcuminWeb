package com.nanoCurcuminWeb.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class CreateUserRequest {
    private String firstName;
    private String lastName;
    private String userName;
    private String email;
    private String password;
    private String address;
    private String phoneNumber;
}
