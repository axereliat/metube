package org.metube.enumeration;

public enum Gender {
    MALE, FEMALE;

    @Override
    public String toString() {
        return this.name().charAt(0) + this.name().substring(1).toLowerCase();
    }
}
