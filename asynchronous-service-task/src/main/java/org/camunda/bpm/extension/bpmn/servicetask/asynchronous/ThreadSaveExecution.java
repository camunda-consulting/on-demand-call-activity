package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.variable.VariableMap;

public class ThreadSaveExecution {

  private final VariableMap variables;
  private final VariableMap variablesLocal;
  private final String id;
  private final RuntimeService runtimeService;

  public ThreadSaveExecution(final DelegateExecution execution) {
    variables = execution.getVariablesTyped();
    variablesLocal = execution.getVariablesLocalTyped();
    id = execution.getId();
    runtimeService = execution.getProcessEngineServices().getRuntimeService();
  }

  public void signal(final Map<String, Object> variables) {
    runtimeService.signal(id, variables);
  }

  public VariableMap getVariables() {
    return variables;
  }

  public VariableMap getVariablesLocal() {
    return variablesLocal;
  }

}
