/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.ui;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import io.avaje.inject.BeanScope;
import io.avaje.jex.Jex;
import io.avaje.jex.Routing;
import java.net.ServerSocket;
import org.ethelred.kv2.MySQLContainerExtension;
import org.ethelred.kv2.controllers.MyExceptionHandlers;
import org.ethelred.kv2.dev.DevConfig;
import org.ethelred.kv2.providers.discord.DiscordApiConfig;
import org.ethelred.kv2.security.AuthFilter;
import org.ethelred.kv2.security.OAuthDiscordConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MySQLContainerExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SuppressWarnings("initialization")
public class LoginUITest {

    private BeanScope scope;
    private Jex.Server server;
    private int port;
    private Playwright playwright;
    private Browser browser;

    @BeforeAll
    public void setUp() throws Exception {
        try (var ss = new ServerSocket(0)) {
            port = ss.getLocalPort();
        }

        // JwtConfig uses the SecurityConfigFactory default — both generate/parse paths use the same key
        scope = BeanScope.builder()
                .profiles("test")
                .bean(OAuthDiscordConfig.class, new OAuthDiscordConfig() {
                    public String clientId() {
                        return "test_discord_id";
                    }

                    public String clientSecret() {
                        return "test_discord_secret";
                    }

                    public String redirectUri() {
                        return "http://localhost:" + port + "/oauth/callback/discord";
                    }

                    public String authorizeUrl() {
                        return "http://localhost:" + port + "/stub/oauth/authorize";
                    }

                    public String tokenUrl() {
                        return "http://localhost:" + port + "/stub/oauth/token";
                    }
                })
                .bean(DiscordApiConfig.class, new DiscordApiConfig() {
                    public String apiBaseUrl() {
                        return "http://localhost:" + port + "/stub/api/v9";
                    }
                })
                .bean(DevConfig.class, new DevConfig() {
                    public boolean enabled() {
                        return true;
                    }
                })
                .build();
        var authFilter = scope.get(AuthFilter.class);
        var exHandlers = scope.get(MyExceptionHandlers.class);

        var app = Jex.create()
                .routing(scope.list(Routing.HttpService.class))
                .before(authFilter::before)
                .port(port);
        exHandlers.configure(app);
        server = app.start();

        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    public void tearDown() {
        browser.close();
        playwright.close();
        server.shutdown();
        scope.close();
    }

    @Test
    public void loginWithStubOAuthShowsLoggedInState() {
        Page page = browser.newPage();
        page.navigate("http://localhost:" + port + "/");

        // Home page initially shows the login prompt
        assertTrue(page.isVisible("text=Log in to start creating rosters."));

        // Open the login modal
        page.click("label[for='login-modal']");

        // Navigate through stub OAuth: Discord → stub authorize → select user
        page.click("text=Login with Discord");
        page.click("text=Login as devuser");

        // Browser follows redirects back to home page
        page.waitForURL("http://localhost:" + port + "/");

        assertFalse(
                page.isVisible("text=Log in to start creating rosters."),
                "Home page should not show login prompt after successful login");
        assertFalse(page.isVisible("label[for='login-modal']"), "Login button should not be visible when logged in");
    }
}
