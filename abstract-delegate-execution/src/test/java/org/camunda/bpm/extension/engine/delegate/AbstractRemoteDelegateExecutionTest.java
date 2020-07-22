package org.camunda.bpm.extension.engine.delegate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.Before;
import org.junit.Test;

public class AbstractRemoteDelegateExecutionTest {

  private static AbstractDelegateExecution execution;
  private Map<String, Object> variables;

  @Before
  public void setUpBeforeTest() throws Exception {
    variables = new HashMap<String, Object>();
    variables.put("myBoolean", true);
    execution = new AbstractRemoteDelegateExecutionTestImpl(variables);
  }

  @Test
  public void testDefaultConstructor() {
    DelegateExecution execution = new AbstractRemoteDelegateExecutionTestImpl();
    assertNotNull(execution);
  }

  @Test
  public void testConstructorMapOfStringObject() {
    assertNotNull(execution);
  }

  @Test
  public void testGetProcessInstanceId() {
    assertNull(execution.getProcessInstanceId());
  }

  @Test
  public void testGetProcessDefinitionId() {
    assertNull(execution.getProcessDefinitionId());
  }

  @Test
  public void testGetCurrentActivityId() {
    assertNull(execution.getCurrentActivityId());
  }

  @Test
  public void testGetActivityInstanceId() {
    assertNull(execution.getActivityInstanceId());
  }

  @Test
  public void testGetTenantId() {
    assertNull(execution.getTenantId());
  }

  @Test
  public void testGetId() {
    assertNull(execution.getId());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetCurrentActivityName() {
    execution.getCurrentActivityName();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetBpmnModelElementInstance() {
    execution.getBpmnModelElementInstance();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetBpmnModelInstance() {
    execution.getBpmnModelInstance();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetProcessEngineServices() {
    execution.getProcessEngineServices();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetProcessEngine() {
    execution.getProcessEngine();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetParentId() {
    execution.getParentId();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetParentActivityInstanceId() {
    execution.getParentActivityInstanceId();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetEventName() {
    execution.getEventName();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetCurrentTransitionId() {
    execution.getCurrentTransitionId();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetProcessInstance() {
    execution.getProcessInstance();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetSuperExecution() {
    execution.getSuperExecution();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testIsCanceled() {
    execution.isCanceled();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateIncidentStringString() {
    execution.createIncident("", "");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testCreateIncidentStringStringString() {
    execution.createIncident("", "", "");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testResolveIncident() {
    execution.resolveIncident("");
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetBusinessKey() {
    execution.getBusinessKey();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testGetProcessBusinessKey() {
    execution.getProcessBusinessKey();
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetProcessBusinessKey() {
    execution.setProcessBusinessKey("");
  }

  @Test
  public void testGetVariables() {
    Map<String, Object> variables2 = execution.getVariables();
    assertEquals(1, variables2.size());
    assertTrue((Boolean) variables2.get("myBoolean"));
  }

  @Test
  public void testGetVariableString() {
    Boolean myBoolean = (Boolean) execution.getVariable("myBoolean");
    assertTrue(myBoolean);
  }

  @Test
  public void testGetVariableLocalString() {
    Boolean myBoolean = (Boolean) execution.getVariableLocal("myBoolean");
    assertTrue(myBoolean);
  }

  @Test
  public void testGetVariableTypedString() {
    Boolean myBoolean = (Boolean) execution.getVariableTyped("myBoolean").getValue();
    assertTrue(myBoolean);
  }

  @Test
  public void testGetVariableLocalTypedString() {
    Boolean myBoolean = (Boolean) execution.getVariableLocalTyped("myBoolean").getValue();
    assertTrue(myBoolean);
  }

  public void testHasVariables() {
    assertTrue(execution.hasVariables());
  }

  @Test
  public void testHasVariablesLocal() {
    assertTrue(execution.hasVariablesLocal());
  }

  @Test
  public void testHasVariable() {
    assertTrue(execution.hasVariable("myBoolean"));
  }

  @Test
  public void testHasVariableLocal() {
    assertTrue(execution.hasVariableLocal("myBoolean"));
  }

  @Test
  public void testSetVariables() {
    Map<String, Object> newVariables = new HashMap<String, Object>();
    newVariables.put("myNewString", "foo");
    execution.setVariables(newVariables);
    assertTrue(execution.hasVariable("myNewString"));
  }

  @Test
  public void testSetVariablesLocal() {
    Map<String, Object> newVariables = new HashMap<String, Object>();
    newVariables.put("myNewString", "foo");
    execution.setVariablesLocal(newVariables);
    assertTrue(execution.hasVariableLocal("myNewString"));
  }

  @Test
  public void testRemoveVariables() {
    execution.removeVariables();
    assertFalse(execution.hasVariable("myBoolean"));
  }

  @Test
  public void testRemoveVariablesLocal() {
    execution.removeVariablesLocal();
    assertFalse(execution.hasVariableLocal("myBoolean"));
  }

  @Test
  public void testRemoveVariablesCollectionOfString() {
    execution.removeVariables(Arrays.asList("myBoolean"));
    assertFalse(execution.hasVariable("myBoolean"));
  }

  @Test
  public void testRemoveVariablesLocalCollectionOfString() {
    execution.removeVariablesLocal(Arrays.asList("myBoolean"));
    assertFalse(execution.hasVariableLocal("myBoolean"));
  }

  @Test
  public void testSetVariableStringObject() {
    execution.setVariable("myBoolean", false);
    assertFalse((Boolean) execution.getVariables().get("myBoolean"));
  }

  @Test(expected = UnsupportedOperationException.class)
  public void testSetVariableStringObjectString() {
    execution.setVariable("myBoolean", false, "myActivity");
  }

  @Test
  public void testSetVariableLocalStringObject() {
    execution.setVariableLocal("myBoolean", false);
    assertFalse((Boolean) execution.getVariables().get("myBoolean"));
  }

  @Test
  public void testRemoveVariableString() {
    execution.removeVariable("myBoolean");
    assertFalse(execution.hasVariable("myBoolean"));
  }

  @Test
  public void testRemoveVariableLocalString() {
    execution.removeVariableLocal("myBoolean");
    assertFalse(execution.hasVariableLocal("myBoolean"));
  }

}
