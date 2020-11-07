package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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
    ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(PROCESS_DEFINITION_KEY);
    assertThat(processInstance).isWaitingAt("AsynchronousServiceTask");
	Thread.sleep(1000L);
    assertThat(processInstance).isEnded();
  }
  
  @Test
  @Deployment(resources = "process.bpmn")
  public void testBpmnSignalEvent() throws InterruptedException {
    assertEquals(2, processDefinitionQuery().count());
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY)
        .setVariable("triggerBpmnSignal", true)
        .execute();
    assertThat(processInstance).isWaitingAt("AsynchronousServiceTask");
    assertEquals(1, processInstanceQuery().count());
    Thread.sleep(1000L);
    assertThat(processInstance).isEnded();
    assertEquals(1, processInstanceQuery().count());
    assertEquals(1, taskService().createTaskQuery().count());
  }

}
