/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

import io.micronaut.data.annotation.MappedEntity;

@MappedEntity
public record DocumentStub(String id, String title) {}
