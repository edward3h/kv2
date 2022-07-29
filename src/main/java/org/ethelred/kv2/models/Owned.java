/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.models;

public interface Owned {
    User owner();

    Visibility visibility();
}
