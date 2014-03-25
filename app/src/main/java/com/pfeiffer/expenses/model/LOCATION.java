package com.pfeiffer.expenses.model;

/**
 * Created by axelpfeiffer on 18.03.14.
 */
public enum LOCATION {
    NONE(""), ALDI("Aldi"), BAKEREI("Bäckerei"), BASIC("Basic"), BUDNI("Budni"), DM("DM"), EDEKA("Edeka"),
    KANTINE("Kantine"), KIOSK("Kiosk"), MARKT("Markt"), PENNY("Penny"), REWE("Rewe"), ROSSMANN("Rossmann"),
    SONSTIGES("Sonstiges"), TANKSTELLE("Tankstelle"), TJARDENS("Tjardens");

    private final String friendlyName;

    private LOCATION(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public static LOCATION fromString(String description) {
        for (LOCATION l : values()) {
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