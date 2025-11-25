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

package ch.ost.hop.pipeline.transforms.wktwkbconverter;

import ch.ost.hop.pipeline.transforms.wktwkbconverter.model.GeometryFormat;
import org.apache.hop.core.Const;
import org.apache.hop.core.exception.HopException;
import org.apache.hop.core.row.RowDataUtil;
import org.apache.hop.i18n.BaseMessages;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.*;

/** Transform That contains the basic skeleton needed to create your own plugin */
public class WktWkbConverter extends BaseTransform<WktWkbConverterMeta, WktWkbConverterData> {

  private static final Class<?> PKG = WktWkbConverter.class; // Needed by Translator

  public WktWkbConverter(
      TransformMeta transformMeta,
      WktWkbConverterMeta meta,
      WktWkbConverterData data,
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

      String realInputField = resolve(meta.getInputField());
      String realOutputField = resolve(meta.getOutputField());

      data.inputFieldIndex = getInputRowMeta().indexOfValue(realInputField);
      if (data.inputFieldIndex < 0)
        throw new HopException("Field " + realInputField + " not found");
      data.inputMeta = data.inputRowMeta.getValueMeta(data.inputFieldIndex);

      data.outputMeta = data.outputRowMeta.searchValueMeta(realOutputField);
      data.outputFieldIndex = data.outputRowMeta.indexOfValue(realOutputField);
      if (data.outputFieldIndex < 0) {
        data.outputFieldIndex = data.outputRowMeta.size() - 1;
      }
    }

    try {
      Object[] outputRow = RowDataUtil.createResizedCopy(inputRow, data.outputRowMeta.size());
      if (meta.getOutputFormat() == GeometryFormat.WKB) {
        String inWKT = Const.NVL(data.inputMeta.getString(inputRow[data.inputFieldIndex]), "");
        outputRow[data.outputFieldIndex] =
            wktToWkb(inWKT, meta.getEndianness(), meta.isAddSRID(), meta.getSrid());
      } else {
        byte[] inWKB = data.inputMeta.getBinary(inputRow[data.inputFieldIndex]);
        outputRow[data.outputFieldIndex] = wkbToWkt(inWKB, meta.isAddSRID(), meta.getSrid());
      }
      putRow(data.outputRowMeta, outputRow);
    } catch (Exception e) {
      throw new HopException(
          BaseMessages.getString(PKG, "WktWkb.FailedToConvert.DialogMessage"), e);
    }
    return true;
  }

  public static byte[] wktToWkb(String wkt, int endianness, boolean addSRID, int newSrid)
      throws Exception {
    String geomStr = wkt;
    int oldSrid = 0;
    if (wkt.contains(";")) {
      String[] parts = wkt.split(";", 2);
      try {
        oldSrid = Integer.parseInt(parts[0].split("=")[1]);
      } catch (NumberFormatException e) {
        throw new HopException("WktWkb.SRIDIncorrectlyFormatted.DialogMessage", e);
      }
      geomStr = parts[1];
      if (addSRID && oldSrid != newSrid) {
        throw new HopException(
            BaseMessages.getString(PKG, "WktWkb.SRIDAlreadyPresent.DialogMessage"));
      }
    }
    Geometry geometry;
    try {
      geometry = new WKTReader().read(geomStr);
    } catch (ParseException e) {
      throw new HopException("WktWkb.GeometryIncorrectlyFormated.DialogMessage" + wkt, e);
    }
    int outputDimension = getDimensions(geometry);
    var byteOrder = getByteOrder(endianness);
    boolean includeSRID = false;
    if (addSRID) {
      geometry.setSRID(newSrid);
      includeSRID = true;
    } else if (oldSrid != 0) {
      geometry.setSRID(oldSrid);
      includeSRID = true;
    }
    return new WKBWriter(outputDimension, byteOrder, includeSRID).write(geometry);
  }

  public static String wkbToWkt(byte[] wkb, boolean addSRID, int newSRID) throws Exception {
    Geometry geometry;
    try {
      geometry = new WKBReader().read(wkb);
    } catch (ParseException e) {
      throw new HopException("WktWkb.GeometryIncorrectlyFormated.DialogMessage", e);
    }
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
      if (hasSRID) {
        throw new HopException(
            BaseMessages.getString(PKG, "WktWkb.SRIDAlreadyPresent.DialogMessage"));
      }
      return formatEWKT(wkt, newSRID);
    }
  }

  private static String formatEWKT(String wkt, int srid) {
    return String.format("SRID=%d;%s", srid, wkt);
  }

  private static int getByteOrder(int endianness) {
    return endianness == 1 ? ByteOrderValues.BIG_ENDIAN : ByteOrderValues.LITTLE_ENDIAN;
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
