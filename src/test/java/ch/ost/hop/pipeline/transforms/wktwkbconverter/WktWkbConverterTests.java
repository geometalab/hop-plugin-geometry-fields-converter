package ch.ost.hop.pipeline.transforms.wktwkbconverter;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class WktWkbConverterTests {

  @Test
  public void testWktToWkb() throws Exception {
    String wkt = "POINT (1 2)";
    byte[] wkb = WktWkbConverter.wktToWkb(wkt, 0, false);

    assertNotNull(wkb);
    assertTrue(wkb.length > 0);
  }

  @Test
  public void testPolygonToWkb() throws Exception {
    String wkt = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
    byte[] wkb =
        hexStringToByteArray(
            "010300000001000000050000000000000000003E4000000000000024400000000000004440000000000000444000000000000034400000000000004440000000000000244000000000000034400000000000003E400000000000002440");

    assertEquals(wkt, WktWkbConverter.wkbToWkt(wkb, true));
  }

  @Test
  public void testEWKTToEWKBandBack() throws Exception {
    String ewkt = "SRID=4326;POINT (-44.3 60.1)";
    assertEquals(ewkt, WktWkbConverter.wkbToWkt(WktWkbConverter.wktToWkb(ewkt, 0, true), true));
  }

  @Test
  public void testFalseWKT() throws ParseException {
    String wkt = "test string you're supposed to fail";
    assertThrows(ParseException.class, () -> WktWkbConverter.wktToWkb(wkt, 0, true));
  }

  public static byte[] hexStringToByteArray(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
