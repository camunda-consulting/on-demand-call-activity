package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.concurrent.CancellationException;

public class ExecutionRolledBackException extends RuntimeException {
  
  private static final long serialVersionUID = 1L;

  public ExecutionRolledBackException(CancellationException e) {
    super(e);
  }

}
