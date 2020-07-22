package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

public interface AsynchronousJavaDelegate {

  /**
   * This method's implementation must call {@link ThreadSaveExecution#complete()}
   * or {@link ThreadSaveExecution#signal(Object)} in a separate thread and with
   * enough delay to let the engine commit the TX
   */
  void execute(ThreadSaveExecution execution);
  
}
