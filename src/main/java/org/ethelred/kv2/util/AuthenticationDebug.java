/* (C) Edward Harman and contributors 2023-2025 */
package org.ethelred.kv2.util;

import io.micronaut.security.authentication.ServerAuthentication;
import jakarta.inject.Singleton;

@Singleton
public class AuthenticationDebug implements DebugMapper<ServerAuthentication> {
    @Override
    public Class<ServerAuthentication> supportedType() {
        return ServerAuthentication.class;
    }

    @Override
    public String inspect(ServerAuthentication object) {
        var builder = new StringBuilder();
        builder.append("ServerAuthentication[name='")
                .append(object.getName())
                .append("',roles=")
                .append(object.getRoles());
        object.getAttributes()
                .forEach((k, v) ->
                        builder.append(",").append(k).append("='").append(v).append("'"));
        builder.append("]");
        return builder.toString();
    }
}
