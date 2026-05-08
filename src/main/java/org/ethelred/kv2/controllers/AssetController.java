/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.controllers;

import io.avaje.http.api.Controller;
import io.avaje.http.api.Get;
import io.avaje.jex.http.ContentType;
import io.avaje.jex.http.Context;
import io.avaje.jex.http.HttpStatus;
import jakarta.inject.Singleton;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/assets")
@Singleton
public class AssetController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AssetController.class);

    private static final Set<String> ROOTS = Set.of("assets", "META-INF/resources/webjars");
    private static final Map<String, ContentType> CONTENT_TYPE_FROM_EXTENSION = Map.of(
            "css", ContentType.TEXT_CSS,
            "js", ContentType.TEXT_JS,
            "png", ContentType.IMAGE_PNG,
            "woff2", ContentType.FONT_WOFF2);

    @Get("/<path>")
    public void getAsset(Context ctx, String path) {
        LOGGER.debug("getAsset({})", path);
        var extension = extension(path);
        LOGGER.debug("extension \"{}\"", extension);
        if (!CONTENT_TYPE_FROM_EXTENSION.containsKey(extension)) {
            ctx.status(HttpStatus.NOT_FOUND_404);
            LOGGER.debug("not found");
            return;
        }
        for (var root : ROOTS) {
            var is = getResource(root + "/" + path);
            LOGGER.debug("Checked \"{}/{}\" {}", root, path, is != null);
            if (is != null) {
                ctx.status(HttpStatus.OK_200);
                ctx.contentType(CONTENT_TYPE_FROM_EXTENSION.get(extension));
                ctx.write(is);
                return;
            }
        }
        ctx.status(HttpStatus.NOT_FOUND_404);
        LOGGER.debug("not found");
    }

    private String extension(String path) {
        var index = path.lastIndexOf(".");
        if (index < 0 || index == path.length() - 1) {
            return "";
        }
        return path.substring(index + 1);
    }

    private @Nullable InputStream getResource(String name) {
        var cl = getClass().getClassLoader();
        return cl == null ? null : cl.getResourceAsStream(name);
    }
}
