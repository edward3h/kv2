/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

public interface RosterParser {
    ParsedRoster parseRoster(String input);
}