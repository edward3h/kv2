/* (C) Edward Harman and contributors 2023-2025 */
package org.ethelred.kv2.services;

import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.ethelred.roster.RosterParser;
import org.ethelred.roster.RosterParserImpl;

@Factory
public class RosterParserFactory {
    @Singleton
    public RosterParser getRosterParser() {
        return new RosterParserImpl();
    }
}
