/* (C) Edward Harman and contributors 2022-2023 */
package org.ethelred.kv2.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
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
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
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
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
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
        var request = HttpRequest.POST("/abc/rosters", body)
                .contentType(MediaType.TEXT_PLAIN)
                .basicAuth("first", "hello");
        var response = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.View.class));
        assertEquals("My First Roster", response.title());
        assertEquals("ABC", response.owner().displayName());
        assertEquals(body, response.body());
    }

    @Test
    public void createRosterNoTitle() {
        var body = """
                Big Detachment
                 Blah
                  Blah
                """;
        var request = HttpRequest.POST("/abc/rosters", body)
                .contentType(MediaType.TEXT_PLAIN)
                .basicAuth("first", "hello");
        var response = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.View.class));
        assertEquals("My Roster", response.title());
        assertEquals("ABC", response.owner().displayName());
    }

    @Test
    public void makeRosterPublic() {
        var body = """
                {"visibility": "PUBLIC"}
                """;
        var request = HttpRequest.PATCH("/abc/rosters/345", body).basicAuth("second", "whatever");
        client.toBlocking().retrieve(request);
        request = HttpRequest.GET("/abc/rosters/345");
        var result = client.toBlocking().retrieve(request, Argument.of(SimpleRoster.class));
        assertEquals("Body goes here", result.body());
    }

    @Test
    public void badPatch() {
        var body = """
                {"missing": "PUBLIC"}
                """;
        var request = HttpRequest.PATCH("/abc/rosters/345", body).basicAuth("second", "whatever");
        var exception = assertThrows(
                HttpClientResponseException.class, () -> client.toBlocking().retrieve(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    public void badPatch2() {
        var body = """
                {"visibility": "POOP"}
                """;
        var request = HttpRequest.PATCH("/abc/rosters/345", body).basicAuth("second", "whatever");
        var exception = assertThrows(
                HttpClientResponseException.class, () -> client.toBlocking().retrieve(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }
}
