/* (C) Edward Harman and contributors 2022 */
package org.ethelred.kv2.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import org.ethelred.kv2.models.DocumentStub;
import org.junit.jupiter.api.Test;

@MicronautTest
public class RosterControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    @Test
    public void listForbidden() {
        var exception = assertThrows(
                HttpClientResponseException.class, () -> client.toBlocking().retrieve("/abc/rosters"));
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void listEmpty() {
        var request = HttpRequest.GET("/abc/rosters").basicAuth("empty", "empty");
        var result = client.toBlocking().retrieve(request, Argument.of(List.class, Argument.of(DocumentStub.class)));
        assertEquals(List.of(), result);
    }
}
