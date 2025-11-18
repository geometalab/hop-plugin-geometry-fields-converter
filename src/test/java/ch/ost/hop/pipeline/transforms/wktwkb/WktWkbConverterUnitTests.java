package ch.ost.hop.pipeline.transforms.wktwkb;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.ParseException;

import static org.junit.jupiter.api.Assertions.*;

public class WktWkbConverterUnitTests {

    @Test
    public void testWktToWkb() throws Exception {
        String wkt = "POINT (1 2)";
        byte[] wkb = WktWkbConverter.wktToWkb(wkt, 0);

        assertNotNull(wkb);
        assertTrue(wkb.length > 0);
    }

    @Test
    public void testWkbToWkt() throws Exception {
        String wkt = "POINT (1 2)";
        byte[] wkb = WktWkbConverter.wktToWkb(wkt, 1);
        String outWkt = WktWkbConverter.wkbToWkt(wkb);

        assertEquals(wkt, outWkt);
    }

    @Test
    public void testFalseWKT() throws Exception {
        String wkt = "(1 2)";
        assertThrows(ParseException.class, () -> WktWkbConverter.wktToWkb(wkt, 0));
    }

}
