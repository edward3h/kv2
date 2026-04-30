/* (C) Edward Harman and contributors 2023-2026 */
package org.ethelred.kv2.util;

import gg.jte.models.runtime.JteModel;
import gg.jte.output.StringOutput;

public class ViewsHelper {
    private ViewsHelper() {}

    public static String render(JteModel model) {
        var output = new StringOutput();
        model.render(output);
        return output.toString();
    }
}
