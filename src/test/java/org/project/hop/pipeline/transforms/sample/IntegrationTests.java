package org.project.hop.pipeline.transforms.sample;

import org.apache.hop.core.HopEnvironment;
import org.apache.hop.pipeline.PipelineMeta;
import org.apache.hop.pipeline.engines.local.LocalPipelineEngine;
import org.apache.hop.pipeline.transform.TransformMeta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class IntegrationTests {

    PipelineMeta pipeline;

    @BeforeEach
    void setUp() throws Exception {
        HopEnvironment.init();

        pipeline = new PipelineMeta();
        pipeline.setName("CSV Validator Integration Test");

        SampleMeta sampleMeta = new SampleMeta();
        sampleMeta.setCsvPath("src/test/resources/email-password-recovery-code.csv");
        TransformMeta input = new TransformMeta("Input", sampleMeta);

        pipeline.addTransform(input);
    }

  @Test
  void testIntegration() throws Exception {
      LocalPipelineEngine engine = new LocalPipelineEngine(pipeline);
      engine.prepareExecution();
      engine.startThreads();
      assertDoesNotThrow(engine::waitUntilFinished);

  }
}
