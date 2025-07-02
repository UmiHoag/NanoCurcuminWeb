package com.nanoCurcuminWeb.enums;

import com.nanoCurcuminWeb.exceptions.InvalidEnumCodeException;
import lombok.Getter;

@Getter
public enum DeletedStatus {
    ACTIVE("ACTV", "ACTIVE"),
    DELETED("DEL", "DELETED");
    private final String code;
    private final String name;

    DeletedStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static DeletedStatus fromCode(String code) {
        for (DeletedStatus status : DeletedStatus.values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        throw new InvalidEnumCodeException("Invalid deleted state: " + code);
    }

    public static String getName(String code) {
        return DeletedStatus.fromCode(code).getName();
    }
}
