/*
 * Copyright 2010-2024 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.test.cases.generated.cases.components.dataFlowInfoProvider;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analysis.api.fir.test.configurators.AnalysisApiFirTestConfiguratorFactory;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfiguratorFactoryData;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiTestConfigurator;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.TestModuleKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.FrontendKind;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisSessionMode;
import org.jetbrains.kotlin.analysis.test.framework.test.configurators.AnalysisApiMode;
import org.jetbrains.kotlin.analysis.api.impl.base.test.cases.components.dataFlowInfoProvider.AbstractExitPointSnapshotTest;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.analysis.api.GenerateAnalysisApiTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot")
@TestDataPath("$PROJECT_ROOT")
public class FirIdeDependentAnalysisScriptSourceModuleExitPointSnapshotTestGenerated extends AbstractExitPointSnapshotTest {
  @NotNull
  @Override
  public AnalysisApiTestConfigurator getConfigurator() {
    return AnalysisApiFirTestConfiguratorFactory.INSTANCE.createConfigurator(
      new AnalysisApiTestConfiguratorFactoryData(
        FrontendKind.Fir,
        TestModuleKind.ScriptSource,
        AnalysisSessionMode.Dependent,
        AnalysisApiMode.Ide
      )
    );
  }

  @Test
  public void testAllFilesPresentInExitPointSnapshot() {
    KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot"), Pattern.compile("^(.+)\\.kts$"), null, true);
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow")
  @TestDataPath("$PROJECT_ROOT")
  public class ControlFlow {
    @Test
    public void testAllFilesPresentInControlFlow() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/conditionalJumps")
    @TestDataPath("$PROJECT_ROOT")
    public class ConditionalJumps {
      @Test
      public void testAllFilesPresentInConditionalJumps() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/conditionalJumps"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/definiteJumps")
    @TestDataPath("$PROJECT_ROOT")
    public class DefiniteJumps {
      @Test
      public void testAllFilesPresentInDefiniteJumps() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/definiteJumps"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/differentTargets")
    @TestDataPath("$PROJECT_ROOT")
    public class DifferentTargets {
      @Test
      public void testAllFilesPresentInDifferentTargets() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/differentTargets"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/exitPointEquivalence")
    @TestDataPath("$PROJECT_ROOT")
    public class ExitPointEquivalence {
      @Test
      public void testAllFilesPresentInExitPointEquivalence() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/exitPointEquivalence"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }

    @Nested
    @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/unconditionalJumps")
    @TestDataPath("$PROJECT_ROOT")
    public class UnconditionalJumps {
      @Test
      public void testAllFilesPresentInUnconditionalJumps() {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/controlFlow/unconditionalJumps"), Pattern.compile("^(.+)\\.kts$"), null, true);
      }
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/defaultValues")
  @TestDataPath("$PROJECT_ROOT")
  public class DefaultValues {
    @Test
    public void testAllFilesPresentInDefaultValues() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/defaultValues"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/languageConstructs")
  @TestDataPath("$PROJECT_ROOT")
  public class LanguageConstructs {
    @Test
    public void testAllFilesPresentInLanguageConstructs() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/languageConstructs"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }

  @Nested
  @TestMetadata("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/variables")
  @TestDataPath("$PROJECT_ROOT")
  public class Variables {
    @Test
    public void testAllFilesPresentInVariables() {
      KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("analysis/analysis-api/testData/components/dataFlowInfoProvider/exitPointSnapshot/variables"), Pattern.compile("^(.+)\\.kts$"), null, true);
    }
  }
}
