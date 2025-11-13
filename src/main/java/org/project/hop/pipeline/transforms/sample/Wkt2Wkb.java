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

package org.project.hop.pipeline.transforms.sample;

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


/**
 * Transform That contains the basic skeleton needed to create your own plugin
 */
public class Wkt2Wkb extends BaseTransform<Wkt2WkbMeta, Wkt2WkbData> {

  private static final Class<?> PKG = Wkt2Wkb.class; // Needed by Translator

  public Wkt2Wkb(
      TransformMeta transformMeta,
      Wkt2WkbMeta meta,
      Wkt2WkbData data,
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
      System.out.println("First");
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

    String in = Const.NVL(data.inputMeta.getString(row[data.inputFieldIndex]), "");
    try {
      row[data.outputFieldIndex] = wktToWkb(in);
    } catch (Exception e) {
      System.out.println("womp womp");
    }
    putRow(data.outputRowMeta, row);

    return true;
  }

  public static byte[] wktToWkb(String wkt) throws Exception {
    WKTReader reader = new WKTReader();
    Geometry geometry = reader.read(wkt);
    WKBWriter writer = new WKBWriter();
    return writer.write(geometry);
  }

  // Convert WKB → WKT
  public static String wkbToWkt(byte[] wkb) throws Exception {
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
