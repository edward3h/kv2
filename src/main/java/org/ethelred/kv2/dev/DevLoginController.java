/* (C) Edward Harman and contributors 2023-2025 */
package org.ethelred.kv2.dev;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.io.Writable;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/dev")
@Requires(env = "dev")
public class DevLoginController {
    @Get("/login")
    @Produces(MediaType.TEXT_HTML)
    @Secured(SecurityRule.IS_ANONYMOUS)
    public Writable loginForm() {
        return writer -> writer.write(
                // language=HTML
                """
<html>
<body>
<form action='/login' method='post'>
<label for='username'>Login as</label>
<input type='text' id='username' name='username'>
<input type='hidden' name='password' value='not_used'
<input type='submit' value='Login'>
</form>
</body>
</html>
""");
    }
}
