package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.engine.delegate.DelegateExecutionDTO;

/**
 * Setters for variables should be used to modify data
 *
 * @author Falko Menge (Camunda)
 */
public class ThreadSaveExecution extends DelegateExecutionDTO implements DelegateExecution {

  private static final long serialVersionUID = 1L;

  protected final RuntimeService runtimeService;
  
  public ThreadSaveExecution(final DelegateExecution execution) {
    super(execution);
    runtimeService = execution.getProcessEngineServices().getRuntimeService();
  }

  public void signal() {
    signal(null);
  }

  public void signal(final Object signalData) {
    runtimeService.signal(getId(), null, signalData, getVariables());
  }

}
