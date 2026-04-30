/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.services;

import io.avaje.inject.Bean;
import io.avaje.inject.Factory;
import org.ethelred.roster.RosterParser;
import org.ethelred.roster.RosterParserImpl;

@Factory
public class RosterParserFactory {
    @Bean
    public RosterParser getRosterParser() {
        return new RosterParserImpl();
    }
}
