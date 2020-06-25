package org.camunda.bpm.extension.engine.delegate;

import java.util.Map;

public class TestAbstractRemoteDelegateExecution extends AbstractRemoteDelegateExecution {

  private static final long serialVersionUID = 1L;

  public TestAbstractRemoteDelegateExecution() {
    super();
  }

  public TestAbstractRemoteDelegateExecution(Map<String, ? extends Object> variables) {
    super(variables);
  }

  @Override
  public String getProcessInstanceId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getProcessDefinitionId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getCurrentActivityId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getActivityInstanceId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getTenantId() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String getId() {
    // TODO Auto-generated method stub
    return null;
  }

}
