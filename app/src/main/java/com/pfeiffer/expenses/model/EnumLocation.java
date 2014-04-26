package com.pfeiffer.expenses.model;

import java.io.Serializable;

/**
 * Created by axelpfeiffer on 18.03.14.
 */
public enum EnumLocation implements Serializable{
    NONE(""), ALDI("Aldi"), BAKEREI("Bäckerei"), BASIC("Basic"), BUDNI("Budni"), DM("DM"), EDEKA("Edeka"),
    KANTINE("Kantine"), KIOSK("Kiosk"), MARKT("Markt"), PENNY("Penny"), REWE("Rewe"), ROSSMANN("Rossmann"),
    SONSTIGES("Sonstiges"), TANKSTELLE("Tankstelle"), TJARDENS("Tjardens");

    private final String friendlyName;

    private EnumLocation(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public static EnumLocation fromString(String description) {
        for (EnumLocation l : values()) {
            if (l.friendlyName.equals(description))
                return l;
        }
        return null;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}