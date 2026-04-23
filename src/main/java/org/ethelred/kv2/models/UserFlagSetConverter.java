/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import java.util.EnumSet;
import java.util.Set;

public class UserFlagSetConverter {
    private UserFlagSetConverter() {}

    public static int fromSet(Set<UserFlag> flags) {
        if (flags == null) {
            return 0;
        }
        return flags.stream().mapToInt(UserFlag::bit).sum();
    }

    public static Set<UserFlag> toSet(int value) {
        if (value == 0) {
            return Set.of();
        }
        var r = EnumSet.noneOf(UserFlag.class);
        for (var flag : UserFlag.values()) {
            if ((flag.bit() & value) > 0) {
                r.add(flag);
            }
        }
        return r;
    }
}
