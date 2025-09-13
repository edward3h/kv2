/* (C) Edward Harman and contributors 2022-2025 */
package org.ethelred.kv2.services;

@FunctionalInterface
public interface IdGenerator {
    String generate();
}
