/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.models;

import io.avaje.jsonb.Json;
import org.ethelred.kv2.viewmodels.StubView;

@Json
public record DocumentStub(String id, String title) implements StubView {}
