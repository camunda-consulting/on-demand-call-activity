package org.camunda.bpm.extension.engine.delegate;

import java.util.Map;

public class TestAbstractDelegateExecution extends AbstractDelegateExecution {

  private static final long serialVersionUID = 1L;

  public TestAbstractDelegateExecution() {
    super();
  }

  public TestAbstractDelegateExecution(Map<String, ? extends Object> variables) {
    super(variables);
  }

}
