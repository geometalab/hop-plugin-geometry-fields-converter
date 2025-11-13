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

/**
 * Meta data for the sample transform.
 */
@Transform(
    id = "Wkt2Wkb",
    name = "i18n::Wkt2Wkb.Name",
    description = "i18n::Wkt2Wkb.Description",
    image = "sample.svg",
    categoryDescription = "Wkt2Wkb.Category",
    documentationUrl = "" /*url to your documentation */)
public class Wkt2WkbMeta extends BaseTransformMeta<Wkt2Wkb, Wkt2WkbData> {

  public static final String SAMPLE_TEXT_FIELD_NAME = "Filepath";

  @HopMetadataProperty(
      key = "input_field",
      injectionKeyDescription = "Wkt2Wkb.Injection.InputField")
  private String inputField = "please select";

  @HopMetadataProperty(
      key = "output_field",
      injectionKeyDescription = "Wkt2Wkb.Injection.OutputField")
  private String outputField = "please select";

  public String getInputField() {
    return inputField;
  }

  public String getOutputField() {
    return outputField;
  }

  public void setInputField(String inputField) {
    this.inputField = inputField;
  }

  public void setOutputField(String outputField) {
    this.outputField = outputField;
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

    IValueMeta extra = null;

    if (!Utils.isEmpty(getOutputField())) {
      extra = new ValueMetaBinary(variables.resolve(getOutputField()));
      extra.setOrigin(name);
      rowMeta.addValueMeta(extra);
    } else {
      if (!Utils.isEmpty(getInputField())) {
        extra = rowMeta.searchValueMeta(variables.resolve(getInputField()));
      }
    }

    if (extra != null) {
      extra.setStorageType(IValueMeta.STORAGE_TYPE_NORMAL);
    }
  }

  @Override
  public void check(
      List<ICheckResult> remarks,
      PipelineMeta pipelineMeta,
      TransformMeta transforminfo,
      IRowMeta prev,
      String[] input,
      String[] output,
      IRowMeta info,
      IVariables variables,
      IHopMetadataProvider metadataProvider) {
    // Checks to perform when validating a transform
  }

  @Override
  public void setDefault() {
    // Set default value for new sample text field
    inputField = "WKT input";
    outputField = "WKB output";
  }
}
