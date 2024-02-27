/* (C) Edward Harman and contributors 2024 */
package org.ethelred.kv2.util;

import gg.jte.Content;
import gg.jte.TemplateOutput;
import gg.jte.output.Utf8ByteOutput;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceContent implements Content {
    private static final ResourceContent EMPTY = new ResourceContent(new ByteArrayInputStream(new byte[0]));
    private final InputStream inputStream;

    public ResourceContent(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public static ResourceContent empty() {
        return EMPTY;
    }

    @Override
    public void writeTo(TemplateOutput output) {
        if (output instanceof Utf8ByteOutput byteOutput) {
            writeBytes(byteOutput);
        } else {
            writeDefault(output);
        }
    }

    private void writeDefault(TemplateOutput output) {
        try (var reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            reader.lines().forEach(line -> {
                output.writeContent(line + "\n");
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeBytes(Utf8ByteOutput byteOutput) {
        try (var buffered = new BufferedInputStream(inputStream)) {
            for (var bytes = buffered.readNBytes(512); bytes.length > 0; bytes = buffered.readNBytes(512)) {
                byteOutput.writeBinaryContent(bytes);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
