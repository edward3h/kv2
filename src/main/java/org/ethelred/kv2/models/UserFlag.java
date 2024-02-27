/* (C) Edward Harman and contributors 2022-2024 */
package org.ethelred.kv2.models;

public enum UserFlag {
    ROLE_USER,
    ROLE_SUPERUSER,
    ACCEPTED_TERMS;

    // don't add more than 31 values LOL
    int bit() {
        return 1 << ordinal();
    }

    boolean isRole() {
        return name().startsWith("ROLE_");
    }
}
