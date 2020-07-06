package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ThreadSaveExecutionTest {
  
  @Rule
  public ProcessEngineRule rule = new ProcessEngineRule("camunda.cfg-no-coverage.xml");

  public static ThreadSaveExecution execution;

  @Before
  public void setUp() throws Exception {
    BpmnModelInstance process = Bpmn.createExecutableProcess("process")
        .startEvent()
        .serviceTask("service_task")
          .camundaClass(ThreadSaveExecutionTestDelegate.class)
        .endEvent().done();

    rule.getRepositoryService()
      .createDeployment()
      .addModelInstance("process.bpmn", process)
      .deploy();
    
    rule.getRuntimeService()
      .createProcessInstanceByKey("process")
      .execute();
  }
  
  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testSetVariableLocal() {
    execution.setVariableLocal("localVariable", "can not be saved by RuntimeService#signal");
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testSetVariablesLocal() {
    Map<String, Object> variables = new HashMap<>();
    variables.put("localVariable", "can not be saved by RuntimeService#signal");
    execution.setVariablesLocal(variables);
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariable() {
    execution.removeVariable("localVariable");
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariableLocal() {
    execution.removeVariableLocal("localVariable");
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariables() {
    execution.removeVariables();
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariablesLocal() {
    execution.removeVariablesLocal();
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariablesCollectionOfString() {
    List<String> variableNames = new ArrayList<>();
    variableNames.add("localVariable");
    execution.removeVariables(variableNames);
  }

  @SuppressWarnings("deprecation")
  @Test(expected = UnsupportedOperationException.class)
  public void testRemoveVariablesLocalCollectionOfString() {
    List<String> variableNames = new ArrayList<>();
    variableNames.add("localVariable");
    execution.removeVariablesLocal(variableNames);
  }

}
