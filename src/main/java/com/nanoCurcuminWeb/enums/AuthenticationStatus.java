package com.nanoCurcuminWeb.enums;

import com.nanoCurcuminWeb.exceptions.InvalidEnumCodeException;
import lombok.Getter;

@Getter
public enum AuthenticationStatus {
    AUTHENTICATED("AUTH", "AUTHENTICATED"),
    NOT_AUTHENTICATED("NAUTH", "NOT_AUTHENTICATED");
    private final String code;
    private final String name;

    AuthenticationStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static AuthenticationStatus fromCode(String code) {
        for (AuthenticationStatus status : AuthenticationStatus.values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new InvalidEnumCodeException("Invalid authentication status: " + code);
    }

    public static String getNameByCode(String code) {
        return fromCode(code).getName();

    }
}


