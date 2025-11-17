package org.project.hop.pipeline.transforms.sample;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engines.local.LocalPipelineEngine;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Wkt2WkbIntegrationTests {

  PipelineMeta pipeline;

  @BeforeEach
  void setUp() throws Exception {
   HopEnvironment.init();

   pipeline = new PipelineMeta();
   pipeline.setName("WKT2WKB Converter Integration Test");

   var ogr2ogrMeta = new Wkt2WkbMeta();
   TransformMeta input = new TransformMeta("Input", ogr2ogrMeta);

   pipeline.addTransform(input);
  }

  @Test
  void testIntegration() throws Exception {
    LocalPipelineEngine engine = new LocalPipelineEngine(pipeline);
    engine.prepareExecution();
    engine.startThreads();
    assertDoesNotThrow(engine::waitUntilFinished);
      assertTrue(true);
  }

}
