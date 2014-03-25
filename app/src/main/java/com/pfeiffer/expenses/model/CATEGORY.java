package com.pfeiffer.expenses.model;

/**
 * Created by axelpfeiffer on 18.03.14.
 */
public enum CATEGORY {
    NONE(""), AUTOBENZIN("Auto + Benzin"), ARZTMEDIZIN("Arzt + Medizin"), BACKWAREN("Backwaren"),
    EXTERN("Externes Essen"), FREIZEITUNTERHALTUNG("Freizeit + Unterhaltung"), GENUSS("Genuss"), KINDER("Kinder"),
    KLEIDUNG("Kleidung"), LEBENSMITTEL("Lebensmittel"), OBST("Obst"), PFLEGEPRODUKTE("Pflegeprodukte"),
    SONSTIGES("Sonstiges");

    private final String friendlyName_;

    private CATEGORY(String friendlyName) {
        this.friendlyName_ = friendlyName;
    }

    public static CATEGORY fromString(String description) {
        for (CATEGORY c : values()) {
            if (c.friendlyName_.equals(description))
                return c;
        }
        return null;
    }

    @Override
    public String toString() {
        return friendlyName_;
    }
}