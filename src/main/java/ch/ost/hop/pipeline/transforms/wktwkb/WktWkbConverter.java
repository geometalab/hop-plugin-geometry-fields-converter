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

package ch.ost.hop.pipeline.transforms.wktwkb;

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
      if (meta.isWktToWkb()) {
        String inWKT = Const.NVL(data.inputMeta.getString(inputRow[data.inputFieldIndex]), "");
        outputRow[data.outputFieldIndex] =
            wktToWkb(inWKT, meta.getEndianness() /*, meta.IncludeSRID()*/);
      } else {
        byte[] inWKB = data.inputMeta.getBinary(inputRow[data.inputFieldIndex]);
        outputRow[data.outputFieldIndex] = wkbToWkt(inWKB /*, meta.IncludeSRID()*/);
      }
      putRow(data.outputRowMeta, outputRow);
    } catch (Exception e) {
      System.out.println(e.getMessage());
      throw new HopException(BaseMessages.getString(PKG, "WktWkb.FailedToConvert.DialogMessage"), e);
    }

    return true;
  }

  public static byte[] wktToWkb(String wkt, int endianness /*, boolean includeSRID*/)
      throws Exception {
    WKTReader reader = new WKTReader();
    Geometry geometry = reader.read(wkt);
    var byteOrder = endianness == 1 ? ByteOrderValues.BIG_ENDIAN : ByteOrderValues.LITTLE_ENDIAN;
    WKBWriter writer = new WKBWriter(2, byteOrder /*, includeSRID*/);
    return writer.write(geometry);
  }

  public static String wkbToWkt(byte[] wkb /*, boolean includeSRID*/) throws Exception {
    WKBReader reader = new WKBReader();
    Geometry geometry = reader.read(wkb);
    WKTWriter writer = new WKTWriter();
    return writer.write(geometry);
  }

  @Override
  public boolean init() {
    return super.init();
  }
}
