package com.nanoCurcuminWeb.enums;

import com.nanoCurcuminWeb.exceptions.InvalidEnumCodeException;
import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("PEN", "PENDING"),
    PROCESSING("PRO", "PROCESSING"),
    SHIPPED("SHIP", "SHIPPED"),
    DELIVERED("DELV", "DELIVERED"),
    CANCELLED("CAN", "CANCELLED");
    private final String code;
    private final String name;

    OrderStatus(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.code.equals(code)) {
                return orderStatus;
            }
        }
        throw new InvalidEnumCodeException("Invalid order status: " + code);
    }
    public static String getNameByCode(String code) {
        return fromCode(code).getName();
    }
}
