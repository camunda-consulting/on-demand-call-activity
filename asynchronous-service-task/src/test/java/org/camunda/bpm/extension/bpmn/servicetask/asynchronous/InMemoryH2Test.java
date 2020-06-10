package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.extension.process_test_coverage.junit.rules.TestCoverageProcessEngineRuleBuilder;
import org.camunda.bpm.engine.test.Deployment;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import static org.camunda.bpm.extension.bpmn.servicetask.asynchronous.ProcessConstants.*;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test {

  public static final MultiThreadedJavaDelegate MULTI_THREADED_JAVA_DELEGATE = new MultiThreadedJavaDelegate();

  public static final Async ASYNC = new Async();
  
  @ClassRule
  @Rule
  public static ProcessEngineRule rule = TestCoverageProcessEngineRuleBuilder.create().build();

  static {
    LogFactory.useSlf4jLogging(); // MyBatis
  }

  @Before
  public void setup() {
    init(rule.getProcessEngine());
  }

  @Test
  @Deployment(resources = "process.bpmn")
  public void testHappyPath() throws InterruptedException {
    Mocks.register("async", ASYNC);
    Mocks.register("multiThreadedJavaDelegate", MULTI_THREADED_JAVA_DELEGATE);
    ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    assertThat(processInstance).isWaitingAt("AsynchronousServiceTask");
	Thread.sleep(500L);
    assertThat(processInstance).isEnded();
  }

}
