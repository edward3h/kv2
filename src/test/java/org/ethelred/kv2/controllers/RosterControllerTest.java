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
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.providers.TestDataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@MicronautTest
public class RosterControllerTest {
    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    TestDataLoader dataLoader;

    @BeforeEach
    public void loadTestTables() {
        dataLoader.load("data/user.csv", "data/identity.csv", "data/simple_roster.csv");
    }

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

    @Test
    public void listFirst() {
        var request = HttpRequest.GET("/abc/rosters").basicAuth("first", "whatever");
        var result = client.toBlocking().retrieve(request, Argument.of(List.class, Argument.of(DocumentStub.class)));
        assertEquals(List.of(new DocumentStub("123", "Test Roster 1")), result);
    }

    @Test
    public void getPublicSignedIn() {
        var request = HttpRequest.GET("/abc/rosters/123").basicAuth("first", "whatever");
        var result = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.class));
        assertEquals("Body goes here", result.body());
    }

    @Test
    public void getPublicAnonymous() {
        var request = HttpRequest.GET("/abc/rosters/123");
        var result = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.class));
        assertEquals("Body goes here", result.body());
    }

    @Test
    public void getPrivateSignedIn() {
        var request = HttpRequest.GET("/abc/rosters/345").basicAuth("second", "whatever");
        var result = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.class));
        assertEquals("Body goes here", result.body());
    }

    @Test
    public void getPrivateAnonymous() {
        var exception = assertThrows(HttpClientResponseException.class, () -> {
            var request = HttpRequest.GET("/abc/rosters/345");
            var result = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.class));
        });
        assertEquals("Forbidden", exception.getMessage());
    }

    @Test
    public void createRosterWithTitle() {
        var body =
                """
                # My First Roster

                Big Detachment
                 Blah
                  Blah
                """;
        var request = HttpRequest.POST("/abc/rosters", body).basicAuth("first", "hello");
        var response = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.View.class));
        assertEquals("My First Roster", response.title());
        assertEquals("ABC", response.owner().displayName());
    }

    @Test
    public void createRosterNoTitle() {
        var body = """
                Big Detachment
                 Blah
                  Blah
                """;
        var request = HttpRequest.POST("/abc/rosters", body).basicAuth("first", "hello");
        var response = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.View.class));
        assertEquals("My Roster", response.title());
        assertEquals("ABC", response.owner().displayName());
    }
}
