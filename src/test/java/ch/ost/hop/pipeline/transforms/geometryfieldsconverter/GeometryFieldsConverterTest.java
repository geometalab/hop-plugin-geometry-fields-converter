package ch.ost.hop.pipeline.transforms.geometryfieldsconverter;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.hop.core.exception.HopException;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.*;

import java.util.Arrays;

public class GeometryFieldsConverterTest {

  @Test
  public void testWKTToGeometry() throws Exception {
    String wkt = "POINT (1 2)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    assertNotNull(geometry);
    assertEquals("POINT (1 2)", GeometryFieldsConverter.geometryToWKT(geometry, false, 0));
  }

  @Test
  public void testEWKTWithSRID() throws Exception {
    String wkt = "POINT (1 2)";
    int srid = 4326;
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, true, srid);
    assertEquals("SRID=4326;POINT (1 2)", outWkt);
  }

  @Test
  public void testEWKTAlreadyHasSRID_AddFalse() throws Exception {
    String ewkt = "SRID=3857;POINT (10 20)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(ewkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, false, 0);
    assertEquals("SRID=3857;POINT (10 20)", outWkt);
  }

  @Test
  public void testMalformedSRIDThrows() {
    String ewkt = "SRID=abcd;POINT (1 2)";
    assertThrows(HopException.class, () -> GeometryFieldsConverter.wktToGeometry(ewkt));
  }

  @Test
  public void testInvalidWKTThrows() {
    String wkt = "INVALIDSTRING";
    assertThrows(HopException.class, () -> GeometryFieldsConverter.wktToGeometry(wkt));
  }

  @Test
  public void testPointWKBRoundtrip() throws Exception {
    Geometry geometry = GeometryFieldsConverter.pcToGeometry(12.34, 56.78);
    byte[] wkbBig = GeometryFieldsConverter.geometryToWKB(geometry, 1, false, 0);
    byte[] wkbLittle = GeometryFieldsConverter.geometryToWKB(geometry, 0, false, 0);

    assertFalse(Arrays.equals(wkbBig, wkbLittle));
    Geometry fromBig = GeometryFieldsConverter.wkbToGeometry(wkbBig);
    Geometry fromLittle = GeometryFieldsConverter.wkbToGeometry(wkbLittle);
    assertEquals(geometry.getCoordinate().getX(), fromBig.getCoordinate().x);
    assertEquals(geometry.getCoordinate().getY(), fromBig.getCoordinate().y);
    assertEquals(geometry.getCoordinate().getX(), fromLittle.getCoordinate().x);
    assertEquals(geometry.getCoordinate().getY(), fromLittle.getCoordinate().y);
  }

  @Test
  public void testInvalidWKBThrows() {
    byte[] invalidWKB = new byte[] {1, 2, 3, 4};
    assertThrows(HopException.class, () -> GeometryFieldsConverter.wkbToGeometry(invalidWKB));
  }

  @Test
  public void testPointCoordinateRoundtrip() {
    double x = 5.5;
    double y = -3.3;
    Geometry geometry = GeometryFieldsConverter.pcToGeometry(x, y);
    double[] coords = GeometryFieldsConverter.geometryToPC(geometry);
    assertEquals(x, coords[0]);
    assertEquals(y, coords[1]);
  }

  @Test
  public void testPointCoordinateWithZ() {
    Coordinate coord = new Coordinate(1, 2, 3);
    Geometry geometry = new GeometryFactory().createPoint(coord);
    double[] coords = GeometryFieldsConverter.geometryToPC(geometry);
    assertEquals(1, coords[0]);
    assertEquals(2, coords[1]);
  }

  @Test
  public void testNullCoordinatesReturnNullGeometry() throws HopException {
    Object[] inputRow = new Object[] {null, null};
    Geometry geometry = null;
    try {
      geometry = GeometryFieldsConverter.pcToGeometry((Double) inputRow[0], (Double) inputRow[1]);
    } catch (NullPointerException e) {
    }
    assertNull(geometry);
  }

  @Test
  public void testPolygonWKTRoundtrip() throws Exception {
    String wkt = "POLYGON ((30 10, 40 40, 20 40, 10 20, 30 10))";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, false, 0);
    assertEquals(wkt, outWkt);
  }

  @Test
  public void testLineStringWKTRoundtrip() throws Exception {
    String wkt = "LINESTRING (0 0, 1 1, 2 2)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(wkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, false, 0);
    assertEquals(wkt, outWkt);
  }

  @Test
  public void testEWKTWithSRIDZero() throws Exception {
    String ewkt = "SRID=0;POINT (7 8)";
    Geometry geometry = GeometryFieldsConverter.wktToGeometry(ewkt);
    String outWkt = GeometryFieldsConverter.geometryToWKT(geometry, false, 0);
    assertEquals("POINT (7 8)", outWkt);
  }
}
