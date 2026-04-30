/* (C) Edward Harman and contributors 2022-2026 */
package org.ethelred.kv2.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import io.avaje.jsonb.JsonType;
import io.avaje.jsonb.Jsonb;
import io.avaje.jsonb.Types;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import javax.sql.DataSource;
import org.ethelred.kv2.MySQLContainerExtension;
import org.ethelred.kv2.models.DocumentStub;
import org.ethelred.kv2.models.SimpleRoster;
import org.ethelred.kv2.providers.TestDataLoader;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.security.JwtService;
import org.ethelred.kv2.services.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MySQLContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ApiRosterControllerTest {
    private BeanScope scope;
    private Jex.Server server;
    private int port;
    private JwtService jwtService;
    private UserService userService;
    private TestDataLoader dataLoader;
    private JsonType<List<DocumentStub>> documentStubListType;
    private JsonType<SimpleRoster.View> rosterViewType;
    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeAll
    public void startServer() {
        scope = BeanScope.builder().profiles("test").build();
        jwtService = scope.get(JwtService.class);
        userService = scope.get(UserService.class);
        dataLoader = new TestDataLoader(scope.get(DataSource.class));
        var jsonb = scope.get(Jsonb.class);
        documentStubListType = jsonb.type(Types.listOf(DocumentStub.class));
        rosterViewType = jsonb.type(SimpleRoster.View.class);

        var authFilter = scope.get(AuthFilter.class);
        var exHandlers = scope.get(org.ethelred.kv2.controllers.MyExceptionHandlers.class);

        var app = Jex.create()
                .routing(scope.list(Routing.HttpService.class))
                .before(authFilter::before)
                .port(0);
        exHandlers.configure(app);
        server = app.start();
        port = server.port();
    }

    @AfterAll
    public void stopServer() {
        server.shutdown();
        scope.close();
    }

    @BeforeEach
    public void loadTestTables() {
        dataLoader.load("data/user.csv", "data/identity.csv", "data/simple_roster.csv");
    }

    private String jwtFor(String identity) {
        var user = userService.findOrCreateUser("test", identity, Map.of("name", identity));
        return jwtService.generate(user);
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private HttpRequest.Builder get(String path) {
        return HttpRequest.newBuilder(uri(path)).GET();
    }

    private HttpRequest.Builder withJwt(HttpRequest.Builder req, String identity) {
        return req.header("Cookie", "JWT_TOKEN=" + jwtFor(identity));
    }

    private HttpResponse<String> send(HttpRequest.Builder req) throws Exception {
        return http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    public void listForbidden() throws Exception {
        var response = send(get("/abc/rosters"));
        assertEquals(401, response.statusCode());
    }

    @Test
    public void listEmpty() throws Exception {
        var response = send(withJwt(get("/abc/rosters"), "empty"));
        assertEquals(200, response.statusCode());
        var result = documentStubListType.fromJson(response.body());
        assertEquals(List.of(), result);
    }

    @Test
    public void listFirst() throws Exception {
        var response = send(withJwt(get("/abc/rosters"), "first"));
        assertEquals(200, response.statusCode());
        var result = documentStubListType.fromJson(response.body());
        assertEquals(List.of(new DocumentStub("123", "Test Roster 1")), result);
    }

    @Test
    public void getPublicSignedIn() throws Exception {
        var response = send(withJwt(get("/abc/rosters/123"), "first"));
        assertEquals(200, response.statusCode());
        assertEquals("Body goes here", rosterViewType.fromJson(response.body()).body());
    }

    @Test
    public void getPublicAnonymous() throws Exception {
        var response = send(get("/abc/rosters/123"));
        assertEquals(200, response.statusCode());
        assertEquals("Body goes here", rosterViewType.fromJson(response.body()).body());
    }

    @Test
    public void getPrivateSignedIn() throws Exception {
        var response = send(withJwt(get("/abc/rosters/345"), "second"));
        assertEquals(200, response.statusCode());
        assertEquals("Body goes here", rosterViewType.fromJson(response.body()).body());
    }

    @Test
    public void getPrivateAnonymous() throws Exception {
        var response = send(get("/abc/rosters/345"));
        assertEquals(403, response.statusCode());
    }

    @Test
    public void createRosterWithTitle() throws Exception {
        var body = """
                # My First Roster

                Big Detachment
                 Blah
                  Blah
                """;
        var req = withJwt(
                HttpRequest.newBuilder(uri("/abc/rosters"))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString(body)),
                "first");
        var response = send(req);
        assertEquals(201, response.statusCode());
        var view = rosterViewType.fromJson(response.body());
        assertEquals("My First Roster", view.title());
        assertEquals("ABC", view.owner().displayName());
        assertEquals(body, view.body());
    }

    @Test
    public void createRosterNoTitle() throws Exception {
        var body = """
                Big Detachment
                 Blah
                  Blah
                """;
        var req = withJwt(
                HttpRequest.newBuilder(uri("/abc/rosters"))
                        .header("Content-Type", "text/plain")
                        .POST(HttpRequest.BodyPublishers.ofString(body)),
                "first");
        var response = send(req);
        assertEquals(201, response.statusCode());
        var view = rosterViewType.fromJson(response.body());
        assertEquals("My Roster", view.title());
        assertEquals("ABC", view.owner().displayName());
    }

    @Test
    public void makeRosterPublic() throws Exception {
        var body = """
                {"visibility": "PUBLIC"}
                """;
        var req = withJwt(
                HttpRequest.newBuilder(uri("/abc/rosters/345"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(body)),
                "second");
        var patchResponse = send(req);
        assertEquals(200, patchResponse.statusCode());

        var getResponse = send(get("/abc/rosters/345"));
        assertEquals(200, getResponse.statusCode());
        assertEquals(
                "Body goes here", rosterViewType.fromJson(getResponse.body()).body());
    }

    @Test
    public void badPatch() throws Exception {
        var body = """
                {"missing": "PUBLIC"}
                """;
        var req = withJwt(
                HttpRequest.newBuilder(uri("/abc/rosters/345"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(body)),
                "second");
        var response = send(req);
        assertEquals(400, response.statusCode());
    }

    @Test
    public void badPatch2() throws Exception {
        var body = """
                {"visibility": "POOP"}
                """;
        var req = withJwt(
                HttpRequest.newBuilder(uri("/abc/rosters/345"))
                        .header("Content-Type", "application/json")
                        .method("PATCH", HttpRequest.BodyPublishers.ofString(body)),
                "second");
        var response = send(req);
        assertEquals(400, response.statusCode());
    }
}
