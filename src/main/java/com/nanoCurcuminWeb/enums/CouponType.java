package com.nanoCurcuminWeb.enums;

import com.nanoCurcuminWeb.exceptions.InvalidEnumCodeException;
import lombok.Getter;

@Getter
public enum CouponType {
    PERCENTAGE("PERC", "PERCENTAGE"),
    FLAT("FLA", "FLAT");
    private final String code;
    private final String name;

    CouponType(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static CouponType fromCode(String code) {
        for (CouponType type : CouponType.values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new InvalidEnumCodeException("Invalid coupon type: " + code);
    }

    public static String getNameByCode(String code) {
        return fromCode(code).getName();
    }
}
