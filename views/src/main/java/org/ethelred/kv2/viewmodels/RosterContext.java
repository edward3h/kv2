/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.viewmodels;

import org.ethelred.roster.ParsedRoster;

public interface RosterContext {
    RosterView roster();

    ParsedRoster parsed();
}
