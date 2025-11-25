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
import org.apache.hop.core.ICheckResult;
import org.apache.hop.core.annotations.Transform;
import org.apache.hop.core.exception.HopTransformException;
import org.apache.hop.core.row.IRowMeta;
import org.apache.hop.core.row.IValueMeta;
import org.apache.hop.core.row.value.ValueMetaBinary;
import org.apache.hop.core.row.value.ValueMetaString;
import org.apache.hop.core.util.Utils;
import org.apache.hop.core.variables.IVariables;
import org.apache.hop.metadata.api.HopMetadataProperty;
import org.apache.hop.metadata.api.IHopMetadataProvider;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransformMeta;
import org.apache.hop.pipeline.transform.TransformMeta;

import java.util.List;

/** Meta data for the sample transform. */
@Transform(
    id = "Wkt/Wkb",
    name = "i18n::WktWkb.Name",
    description = "i18n::WktWkb.Description",
    image = "sample.svg",
    categoryDescription = "WktWkb.Category",
    documentationUrl =
        "https://gitlab.ost.ch/apache-hop-plugin-sa/apache-hop-plugins-wkt-wkb-converter/-/blob/main/README.md")
public class WktWkbConverterMeta extends BaseTransformMeta<WktWkbConverter, WktWkbConverterData> {

  @HopMetadataProperty(key = "input_field", injectionKeyDescription = "WktWkb.Injection.InputField")
  private String inputField = "";

  @HopMetadataProperty(
      key = "output_field",
      injectionKeyDescription = "WktWkb.Injection.OutputField")
  private String outputField = "";

  @HopMetadataProperty(key = "from_format", injectionKeyDescription = "WktWkb.Injection.FromFormat")
  private GeometryFormat fromFormat;

  @HopMetadataProperty(key = "to_format", injectionKeyDescription = "WktWkb.Injection.ToFormat")
  private GeometryFormat toFormat;

  /* Endianness in WKB is defined by its first byte, 1: big endian, 2: little endian */
  @HopMetadataProperty(key = "endianness", injectionKeyDescription = "WktWkb.Injection.Endianness")
  private int endianness = 1;

  @HopMetadataProperty(key = "add_srid", injectionKeyDescription = "WktWkb.Injection.AddSRID")
  private boolean addSRID = false;

  @HopMetadataProperty(key = "srid", injectionKeyDescription = "WktWkb.Injection.SRID")
  private int srid = 0;

  public String getInputField() {
    return inputField;
  }

  public void setInputField(String inputField) {
    this.inputField = inputField;
  }

  public String getOutputField() {
    return outputField;
  }

  public void setOutputField(String outputField) {
    this.outputField = outputField;
  }

  public GeometryFormat getFromFormat() {
    return fromFormat;
  }

  public void setFromFormat(GeometryFormat fromFormat) {
    this.fromFormat = fromFormat;
  }

  public GeometryFormat getToFormat() {
    return toFormat;
  }

  public void setToFormat(GeometryFormat toFormat) {
    this.toFormat = toFormat;
  }

  public int getEndianness() {
    return endianness;
  }

  public void setEndianness(int endianness) {
    this.endianness = endianness;
  }

  public boolean isAddSRID() {
    return addSRID;
  }

  public void setAddSRID(boolean addSRID) {
    this.addSRID = addSRID;
  }

  public int getSrid() {
    return srid;
  }

  public void setSrid(int srid) {
    this.srid = srid;
  }

  @Override
  public boolean supportsErrorHandling() {
    return true;
  }

  @Override
  public void getFields(
      IRowMeta rowMeta,
      String name,
      IRowMeta[] info,
      TransformMeta nextTransform,
      IVariables variables,
      IHopMetadataProvider metadataProvider)
      throws HopTransformException {

    IValueMeta extra;
    String resolvedOutputField = variables.resolve(getOutputField());

    if (!Utils.isEmpty(getOutputField())) {
      int outputFieldIndex = rowMeta.indexOfValue(resolvedOutputField);
      extra =
          getToFormat() == GeometryFormat.WKB
              ? new ValueMetaBinary(resolvedOutputField)
              : new ValueMetaString(resolvedOutputField);
      extra.setOrigin(name);
      if (outputFieldIndex < 0) {
        rowMeta.addValueMeta(extra);
      } else {
        rowMeta.setValueMeta(outputFieldIndex, extra);
      }
    } else {
      extra =
              getToFormat() == GeometryFormat.WKB ? new ValueMetaBinary("geometry_wkb") : new ValueMetaString("geometry_wkt");
      extra.setOrigin(getToFormat() == GeometryFormat.WKB ? "geometry_wkb" : "geometry_wkt");
      rowMeta.addValueMeta(extra);
    }

    extra.setStorageType(IValueMeta.STORAGE_TYPE_NORMAL);
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transformInfo,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    // Checks to perform when validating a transform
  }
}
