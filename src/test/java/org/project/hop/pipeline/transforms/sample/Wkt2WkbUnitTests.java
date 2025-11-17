package org.project.hop.pipeline.transforms.sample;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Wkt2WkbUnitTests {

    @Test
    public void testWktToWkb() throws Exception {
        String wkt = "POINT (1 2)";
        byte[] wkb = Wkt2Wkb.wktToWkb(wkt);

        assertNotNull(wkb);
        assertTrue(wkb.length > 0);
    }

    @Test
    public void testWkbToWkt() throws Exception {
        String wkt = "POINT (1 2)";
        byte[] wkb = Wkt2Wkb.wktToWkb(wkt);
        String outWkt = Wkt2Wkb.wkbToWkt(wkb);

        assertEquals(wkt, outWkt);
    }

}
