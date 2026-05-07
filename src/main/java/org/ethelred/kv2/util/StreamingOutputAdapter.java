/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.util;

import gg.jte.Content;
import gg.jte.TemplateOutput;
import io.avaje.http.api.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class StreamingOutputAdapter implements StreamingOutput {
    private final Content content;

    public StreamingOutputAdapter(Content content) {
        this.content = content;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        content.writeTo(new TemplateOutput() {
            @Override
            public void writeContent(String value) {
                try {
                    outputStream.write(value.getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void writeContent(String value, int beginIndex, int endIndex) {
                try {
                    outputStream.write(value.substring(beginIndex, endIndex).getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            @Override
            public void writeBinaryContent(byte[] value) {
                try {
                    outputStream.write(value);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
    }

    public static StreamingOutput streaming(Content content) {
        return new StreamingOutputAdapter(content);
    }
}
