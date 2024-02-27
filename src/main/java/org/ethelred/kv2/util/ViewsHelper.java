/* (C) Edward Harman and contributors 2023-2024 */
package org.ethelred.kv2.util;

import gg.jte.models.runtime.JteModel;
import gg.jte.output.WriterOutput;
import io.micronaut.core.io.Writable;

public class ViewsHelper {
    private ViewsHelper() {}

    public static Writable writable(JteModel model) {
        return writer -> model.render(new WriterOutput(writer));
    }
}
