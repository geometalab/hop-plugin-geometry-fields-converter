/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ost.hop.pipeline.transforms.geometryfieldsconverter;

import static ch.ost.hop.pipeline.transforms.geometryfieldsconverter.model.GeometryFormat.POINT_COORDINATE;

import java.util.Arrays;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ByteOrderValues;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

public class GeometryFieldsConverter
    extends BaseTransform<GeometryFieldsConverterMeta, GeometryFieldsConverterData> {

  private static final Class<?> PKG = GeometryFieldsConverterMeta.class;

  public GeometryFieldsConverter(
      TransformMeta transformMeta,
      GeometryFieldsConverterMeta meta,
      GeometryFieldsConverterData data,
      int copyNr,
      PipelineMeta pipelineMeta,
      Pipeline pipeline) {
    super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
  }

  @Override
  public boolean processRow() throws HopException {
    Object[] inputRow = getRow();
    if (inputRow == null) {
      setOutputDone();
      return false;
    }

    if (first) {
      first = false;

      data.inputRowMeta = getInputRowMeta().clone();
      data.outputRowMeta = getInputRowMeta().clone();

      meta.getFields(data.outputRowMeta, getTransformName(), null, null, this, metadataProvider);

      initializeInputFields();
      initializeOutputFields();
    }

    try {
      Object[] outputRow = RowDataUtil.createResizedCopy(inputRow, data.outputRowMeta.size());
      Geometry geometry = getGeometry(inputRow);

      if (geometry == null) {
        if (meta.getOutputFormat() == POINT_COORDINATE) {
          outputRow[data.outputFieldIndex] = null;
          outputRow[data.yOutputFieldIndex] = null;
        } else {
          outputRow[data.outputFieldIndex] = null;
        }
      } else {
        switch (meta.getOutputFormat()) {
          case WKT:
            if (meta.isAddSRID() && geometry.getSRID() != meta.getSrid()) {
              logBasic(
                  BaseMessages.getString(
                      PKG, "GeometryFields.SRIDAlreadyPresent.Log", geometry.toText()));
            }
            outputRow[data.outputFieldIndex] =
                geometryToWKT(geometry, meta.isAddSRID(), meta.getSrid());
            break;
          case WKB:
            if (meta.isAddSRID() && geometry.getSRID() != meta.getSrid()) {
              logBasic(
                  BaseMessages.getString(
                      PKG, "GeometryFields.SRIDAlreadyPresent.Log", geometry.toText()));
            }
            outputRow[data.outputFieldIndex] =
                geometryToWKB(geometry, meta.getEndianness(), meta.isAddSRID(), meta.getSrid());
            break;
          case POINT_COORDINATE:
            if (is2DPoint(geometry)) {
              double[] pointCoordinates = geometryToPC(geometry);
              outputRow[data.outputFieldIndex] = pointCoordinates[0];
              outputRow[data.yOutputFieldIndex] = pointCoordinates[1];
            } else {
              logBasic(
                  BaseMessages.getString(
                      PKG, "GeometryFields.IneligibleForPC.Error", geometry.toText()));
              return true;
            }
            break;
        }
      }
      putRow(data.outputRowMeta, outputRow);
    } catch (HopException e) {
      throw new HopTransformException(
          BaseMessages.getString(PKG, "GeometryFields.FailedToConvert.DialogMessage"), e);
    }
    return true;
  }

  private void initializeInputFields() throws HopTransformException {
    String realInputField = resolve(meta.getInputField());
    String realYInputField = resolve(meta.getYInputField());

    data.inputFieldIndex = getInputRowMeta().indexOfValue(realInputField);
    if (data.inputFieldIndex < 0)
      throw new HopTransformException(
          BaseMessages.getString(
              PKG, "GeometryFieldsConverter.Error.UnknownInputField", realInputField));
    data.inputMeta = data.inputRowMeta.getValueMeta(data.inputFieldIndex);

    if (meta.getInputFormat() == POINT_COORDINATE) {
      data.yInputFieldIndex = getInputRowMeta().indexOfValue(realYInputField);
      if (data.yInputFieldIndex < 0)
        throw new HopTransformException(
            BaseMessages.getString(
                PKG, "GeometryFieldsConverter.Error.UnknownInputField", realYInputField));
      data.yInputMeta = data.inputRowMeta.getValueMeta(data.yInputFieldIndex);
    }
  }

  private void initializeOutputFields() {
    String realOutputField = resolve(meta.getOutputField());
    String realYOutputField = resolve(meta.getYOutputField());

    data.outputMeta = data.outputRowMeta.searchValueMeta(realOutputField);
    data.outputFieldIndex = data.outputRowMeta.indexOfValue(realOutputField);

    if (meta.getOutputFormat() == POINT_COORDINATE) {
      initializePointCoordinateOutputFields(realYOutputField);
    } else {
      data.yOutputFieldIndex = -1;
      data.yOutputMeta = null;
      if (data.outputFieldIndex < 0) {
        data.outputFieldIndex = data.outputRowMeta.size() - 1;
      }
    }
  }

  private void initializePointCoordinateOutputFields(String realYOutputField) {
    data.yOutputMeta = data.outputRowMeta.searchValueMeta(realYOutputField);
    data.yOutputFieldIndex = data.outputRowMeta.indexOfValue(realYOutputField);

    if (data.outputFieldIndex < 0 && data.yOutputFieldIndex < 0) {
      data.outputFieldIndex = data.outputRowMeta.size() - 2;
      data.yOutputFieldIndex = data.outputRowMeta.size() - 1;
    } else if (data.outputFieldIndex >= 0 && data.yOutputFieldIndex < 0) {
      data.yOutputFieldIndex = data.outputRowMeta.size() - 1;
    } else if (data.outputFieldIndex < 0) {
      data.outputFieldIndex = data.outputRowMeta.size() - 1;
    }
  }

  private Geometry getGeometry(Object[] inputRow) throws HopException {
    Geometry geometry = null;
    try {
      switch (meta.getInputFormat()) {
        case WKT:
          String inWKT = data.inputMeta.getString(inputRow[data.inputFieldIndex]);
          if (inWKT == null || inWKT.isEmpty()) {
            return null;
          }
          geometry = wktToGeometry(inWKT);
          break;
        case WKB:
          byte[] inWKB = data.inputMeta.getBinary(inputRow[data.inputFieldIndex]);
          if (inWKB == null) {
            return null;
          }
          geometry = wkbToGeometry(inWKB);
          break;
        case POINT_COORDINATE:
          Object xObj = inputRow[data.inputFieldIndex];
          Object yObj = inputRow[data.yInputFieldIndex];
          if (xObj == null || yObj == null) {
            return null;
          }
          double x, y;
          try {
            x = data.inputMeta.getNumber(inputRow[data.inputFieldIndex]);
            y = data.yInputMeta.getNumber(inputRow[data.yInputFieldIndex]);
          } catch (HopException e) {
            return null;
          }
          geometry = pcToGeometry(x, y);
          break;
      }
    } catch (Exception e) {
      throw new HopException(e);
    }
    return geometry;
  }

  public static Geometry wktToGeometry(String wkt) throws HopException {
    String geomStr = wkt;
    int srid = 0;
    if (wkt.contains(";")) {
      String[] parts = wkt.split(";", 2);
      try {
        srid = Integer.parseInt(parts[0].split("=")[1]);
      } catch (NumberFormatException e) {
        throw new HopException(
            BaseMessages.getString(
                PKG, "GeometryFields.SRIDIncorrectlyFormatted.DialogMessage", wkt),
            e);
      }
      geomStr = parts[1];
    }
    Geometry geometry;
    try {
      geometry = new WKTReader().read(geomStr);
    } catch (ParseException e) {
      throw new HopException(
          BaseMessages.getString(
              PKG, "GeometryFields.GeometryIncorrectlyFormated.DialogMessage", wkt),
          e);
    }
    if (srid != 0) {
      geometry.setSRID(srid);
    }
    return geometry;
  }

  public static Geometry wkbToGeometry(byte[] wkb) throws HopException {
    Geometry geometry;
    try {
      geometry = new WKBReader().read(wkb);
    } catch (ParseException e) {
      throw new HopException(
          BaseMessages.getString(
              PKG,
              "GeometryFields.GeometryIncorrectlyFormated.DialogMessage",
              Arrays.toString(wkb)),
          e);
    }
    return geometry;
  }

  public static Geometry pcToGeometry(double x, double y) {
    Geometry geometry;
    geometry = new GeometryFactory().createPoint(new Coordinate(x, y));
    return geometry;
  }

  public static String geometryToWKT(Geometry geometry, boolean addSRID, int newSRID) {
    int outputDimension = getDimensions(geometry);
    String wkt = new WKTWriter(outputDimension).write(geometry);
    int currentSRID = geometry.getSRID();
    boolean hasSRID = currentSRID != 0;
    if (!addSRID) {
      if (hasSRID) {
        return formatEWKT(wkt, currentSRID);
      } else {
        return wkt;
      }
    } else {
      return formatEWKT(wkt, newSRID);
    }
  }

  public static byte[] geometryToWKB(
      Geometry geometry, int endianness, boolean addSRID, int newSRID) {
    int outputDimension = getDimensions(geometry);
    boolean hasSRID = geometry.getSRID() != 0;
    var byteOrder = getByteOrder(endianness);
    if (addSRID) {
      geometry.setSRID(newSRID);
    }
    return new WKBWriter(outputDimension, byteOrder, hasSRID || addSRID).write(geometry);
  }

  public static double[] geometryToPC(Geometry geometry) {
    Coordinate coordinate = geometry.getCoordinate();
    return new double[] {coordinate.getX(), coordinate.getY()};
  }

  private static String formatEWKT(String wkt, int srid) {
    return String.format("SRID=%d;%s", srid, wkt);
  }

  private static int getByteOrder(int endianness) {
    return endianness == 1 ? ByteOrderValues.BIG_ENDIAN : ByteOrderValues.LITTLE_ENDIAN;
  }

  private static boolean is2DPoint(Geometry geometry) {
    return geometry.getGeometryType().equals(Geometry.TYPENAME_POINT)
        && Double.isNaN(geometry.getCoordinate().getZ())
        && Double.isNaN(geometry.getCoordinate().getM());
  }

  private static int getDimensions(Geometry geometry) {
    return geometry
        .getFactory()
        .getCoordinateSequenceFactory()
        .create(geometry.getCoordinates())
        .getDimension();
  }

  @Override
  public boolean init() {
    return super.init();
  }
}
