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
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

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
    Object[] row = getRow();
    if (row == null) {
      System.out.println("Done!");
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
      data.inputMeta = data.inputRowMeta.getValueMeta(data.inputFieldIndex);

      data.outputMeta = data.outputRowMeta.searchValueMeta(realOutputField);
      data.outputFieldIndex = data.outputRowMeta.size() - 1;
    }

    if(meta.isWktToWkb()) {
      String inWKT = Const.NVL(data.inputMeta.getString(row[data.inputFieldIndex]), "");

      try {
        row[data.outputFieldIndex] = wktToWkb(inWKT, meta.getEndianness()/*, meta.IncludeSRID()*/);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
      putRow(data.outputRowMeta, row);
    }else{

      byte[] inWKB = data.inputMeta.getBinary(row[data.inputFieldIndex]);
      try {
        row[data.outputFieldIndex] = wkbToWkt(inWKB/*, meta.IncludeSRID()*/);
      } catch (Exception e) {
        System.out.println(e.getMessage());
      }
      putRow(data.outputRowMeta, row);

    }
    return true;
  }

  public static byte[] wktToWkb(String wkt, int endianness/*, boolean includeSRID*/) throws Exception {
    WKTReader reader = new WKTReader();
    Geometry geometry = reader.read(wkt);
    byte byteOrder = (byte) (endianness == 0 ? 0x00 : 0x01);
    WKBWriter writer = new WKBWriter(2, byteOrder/*, includeSRID*/);
    return writer.write(geometry);
  }

  public static String wkbToWkt(byte[] wkb/*, boolean includeSRID*/) throws Exception {
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
