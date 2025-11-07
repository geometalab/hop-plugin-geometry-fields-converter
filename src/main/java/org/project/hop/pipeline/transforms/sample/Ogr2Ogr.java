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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * Transform That contains the basic skeleton needed to create your own plugin
 */
public class Ogr2Ogr extends BaseTransform<Ogr2OgrMeta, Ogr2OgrData> {

    private static final Class<?> PKG = Ogr2Ogr.class; // Needed by Translator

    public Ogr2Ogr(
            TransformMeta transformMeta,
            Ogr2OgrMeta meta,
            Ogr2OgrData data,
            int copyNr,
            PipelineMeta pipelineMeta,
            Pipeline pipeline) {
        super(transformMeta, meta, data, copyNr, pipelineMeta, pipeline);
    }

    @Override
    public boolean processRow() throws HopException {
        if (first) {
            System.out.println("Getting ready");

            first = false;

            String[] command = { "ogr2ogr", "-f", "\"GPKG\"", "output.gpkg", "castles_ch.geojson"}; // use "cmd.exe", "/c", "dir" on Windows

            // Create the ProcessBuilder
            ProcessBuilder pb = new ProcessBuilder(command);

            // Set the working directory
            pb.directory(new java.io.File("C:\\Users\\tobia\\Downloads\\Daten_Apache_Hop_Gis\\lehrmittel\\data_cleansing_integration\\gis_data"));

            pb.redirectErrorStream(true);

            try {
                Process process = pb.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream())
                );

                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }

                int exitCode = process.waitFor();
                System.out.println("Exit code: " + exitCode);

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            setOutputDone();
            return false;
        }

        return true;
    }

    @Override
    public boolean init() {
        return  super.init();
    }
}
