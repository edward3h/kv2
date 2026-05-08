/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.util;

import gg.jte.Content;
import io.avaje.http.api.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

public class StreamingOutputAdapter implements StreamingOutput {
    private final Content content;

    public StreamingOutputAdapter(Content content) {
        this.content = content;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        content.writeTo(new OutputStreamTemplateOutput(outputStream));
    }

    public static StreamingOutput streaming(Content content) {
        return new StreamingOutputAdapter(content);
    }
}
