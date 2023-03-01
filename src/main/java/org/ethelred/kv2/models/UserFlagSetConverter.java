/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.models;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.data.model.runtime.convert.AttributeConverter;
import jakarta.inject.Singleton;
import java.util.EnumSet;
import java.util.Set;

@Singleton
public class UserFlagSetConverter implements AttributeConverter<Set<UserFlag>, Integer> {
    @Override
    public Integer convertToPersistedValue(Set<UserFlag> entityValue, ConversionContext context) {
        if (entityValue == null) {
            return 0;
        }
        return entityValue.stream().mapToInt(UserFlag::bit).sum();
    }

    @Override
    public Set<UserFlag> convertToEntityValue(Integer persistedValue, ConversionContext context) {
        if (persistedValue == null) {
            return Set.of();
        }
        var r = EnumSet.noneOf(UserFlag.class);
        for (var flag : UserFlag.values()) {
            if ((flag.bit() & persistedValue) > 0) {
                r.add(flag);
            }
        }
        return r;
    }
}
