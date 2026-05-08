/* (C) Edward Harman and contributors 2026 */
package org.ethelred.kv2.util;

import gg.jte.TemplateOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

class OutputStreamTemplateOutput implements TemplateOutput {
    private final OutputStream outputStream;

    public OutputStreamTemplateOutput(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

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
}
