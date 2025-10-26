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

import org.apache.hop.core.exception.HopException;
import org.apache.hop.pipeline.Pipeline;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.transform.BaseTransform;
import org.apache.hop.pipeline.transform.TransformMeta;
import uk.gov.nationalarchives.csv.validator.api.java.CsvValidator;
import uk.gov.nationalarchives.csv.validator.api.java.FailMessage;
import uk.gov.nationalarchives.csv.validator.api.java.Substitution;

import java.util.ArrayList;
import java.util.List;


/**
 * Transform That contains the basic skeleton needed to create your own plugin
 */
public class Sample extends BaseTransform<SampleMeta, SampleData> {

    private static final Class<?> PKG = Sample.class; // Needed by Translator

    public Sample(
            TransformMeta transformMeta,
            SampleMeta meta,
            SampleData data,
            int copyNr,
            PipelineMeta pipelineMeta,
            Pipeline pipeline) {
        super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    }

    @Override
    public boolean processRow() throws HopException {
        if (first) {
            first = false;


            // If there is no input row meta (no upstream transforms), create one
            if (getInputRowMeta() == null) {
                data.outputRowMeta = new org.apache.hop.core.row.RowMeta();
                getMeta().getFields(data.outputRowMeta, getTransformName(), null, null, this, getMetadataProvider());
            } else {
                data.outputRowMeta = getInputRowMeta().clone();
            }

            System.out.println("Doing shit");
            System.out.println(meta.getSchemaPath());
            ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
            Substitution sub = new Substitution("file://","//");
            List<FailMessage> failMessages =  CsvValidator.validate(meta.getCSVPath(), "C:/Users/tobia/Desktop/test.csvs", false, substitutions, false, false);
            System.out.println("failMessage is empty:");
            System.out.println(failMessages.isEmpty());
            for(FailMessage fm : failMessages){
                System.out.println(fm.getMessage());
            }


            // Create one row with the constant value
            Object[] outputRow = new Object[1];
            outputRow[0] = meta.getCSVPath();

            // Send to output
            putRow(data.outputRowMeta, outputRow);
        } else {
            setOutputDone();
            return false;
        }

        return true;
    }
}
