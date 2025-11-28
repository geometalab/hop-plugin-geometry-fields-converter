package ch.ost.hop.pipeline.transforms.geometryfieldsconverter;

import org.apache.hop.core.exception.HopException;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Geometry;

import static org.junit.jupiter.api.Assertions.*;

public class GeometryFieldsConverterTests {

  @Test
  public void testWKTToGeometry() throws Exception {
    String wkt = "POINT (1 2)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    byte[] wkb = GeometryFieldsConverter.geometryToWKB(geometry, 0, false, 0);

    assertNotNull(wkb);
    assertTrue(wkb.length > 0);
  }

  @Test
  public void testEWKTWithSRID() throws Exception {
    String wkt = "POINT (1 2)";
    int srid = 4296;
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, true, srid);

    assertNotNull(outWkt);
    assertEquals("SRID=4296;POINT (1 2)", outWkt);
  }

  @Test
  public void testPolygonToWkb() throws Exception {
    String wkt = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
    byte[] wkb =
        hexStringToByteArray(
            "010300000001000000050000000000000000003E4000000000000024400000000000004440000000000000444000000000000034400000000000004440000000000000244000000000000034400000000000003E400000000000002440");
    Geometry geometry = GeometryFieldsConverter.wkbToGeometry(wkb);

    assertEquals(wkt, GeometryFieldsConverter.geometryToWKT(geometry, false, 0));
  }

  @Test
  public void testEWKTToEWKBAndBack() throws Exception {
    String ewkt = "SRID=4326;POINT (-44.3 60.1)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(ewkt);
    String result = GeometryFieldsConverter.geometryToWKT(geometry, false, 0);
    assertEquals(ewkt, result);
  }

  @Test
  public void testFalseWKT() {
    String wkt = "test string you're supposed to fail";
    assertThrows(HopException.class, () -> GeometryFieldsConverter.wktToGeometry(wkt));
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
