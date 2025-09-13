/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.models;

import io.micronaut.data.annotation.MappedEntity;
import org.ethelred.kv2.viewmodels.StubView;

@MappedEntity
public record DocumentStub(String id, String title) implements StubView {}
