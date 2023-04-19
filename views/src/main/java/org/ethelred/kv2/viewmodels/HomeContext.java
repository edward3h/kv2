/* (C) Edward Harman and contributors 2023 */
package org.ethelred.kv2.viewmodels;

import java.util.List;

public interface HomeContext {
    List<? extends StubView> rosters();
}
