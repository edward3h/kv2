/* (C) Edward Harman and contributors 2023 */
package org.ethelred.roster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RosterParserTest {
    final RosterParser parser = new RosterParserImpl();

    @Test
    public void level1() {
        var body = """
               Parent
                Child
                """;
        var parsed = parser.parseRoster(body);
        assertEquals(0, parsed.getStyles().length);
        var root = parsed.getRoot();
        assertLevel(root, 1, 0);
        assertLevel(root.getChildren()[0], 1, 0, "Parent");
        assertLevel(root.getChildren()[0].getChildren()[0], 0, 0, "Child");
    }

    @Test
    public void sample1() {
        var body =
                """
# Furnace Fraggers (Wastes)

Gang

Vehicles
 Road Thug [25]
  Mauler [100]
   Twin-linked Bolters [65]

 Road Thug [25]
  Heavy Vehicle [175]
   Wheeled
   Transport Bed [15]
   Nitro Burners [15]
   Heavy Stubber (Crew, Front/Right) [130]
                """;
        var parsed = parser.parseRoster(body);
        assertEquals(0, parsed.getStyles().length);
        var root = parsed.getRoot();
        assertLevel(root, 3, 550);
        var l1Children = root.getChildren();
        assertHeader(l1Children[0], 1);
        assertLevel(l1Children[1], 0, 0);
        assertLevel(l1Children[2], 2, 550);
    }

    private void assertLevel(Level l, int children, int total) {
        assertLevel(l, children, total, null);
    }

    private void assertLevel(Level l, int children, int total, String contains) {
        assertEquals(children, l.getChildren().length);
        assertEquals(total, l.getTotal());
        if (contains != null) {
            assertTrue(l.getText().contains(contains));
        }
    }

    private void assertHeader(Level l, int header) {
        assertNotNull(l.getHeader());
        assertEquals(header, l.getHeader());
    }
}
