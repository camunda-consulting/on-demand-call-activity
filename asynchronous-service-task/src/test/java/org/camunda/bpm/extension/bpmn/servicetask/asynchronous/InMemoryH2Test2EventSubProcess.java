package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.runtime.Execution;
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
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.assertThat;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.execute;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.executionQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.jobQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processInstanceQuery;
import static org.camunda.bpm.engine.test.assertions.bpmn.BpmnAwareTests.runtimeService;
import static org.junit.Assert.*;

import java.util.List;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class InMemoryH2Test2EventSubProcess {

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
  @Deployment(resources = "process-with-event-handler.bpmn")
  public void testBpmnSignalEventSubProcess() throws InterruptedException {
    ProcessInstance processInstance = runtimeService()
        .createProcessInstanceByKey(PROCESS_DEFINITION_KEY + "-with-event-handler")
        .setVariable("triggerBpmnSignal", true)
        .execute();
    List<Execution> executions = executionQuery().list();
    assertThat(processInstance).isWaitingAt("AsynchronousServiceTask");
    Thread.sleep(1000L);
    assertEquals(1, processInstanceQuery().count());
    execute(jobQuery().singleResult());
    assertThat(processInstance).isEnded();
  }

}
