package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.Map;

public interface AsynchronousJavaDelegate {

  /**
   * This method's implementation must call {@link ThreadSaveExecution#signal(Map)}
   * in another thread and with enough delay to let the engine commit the TX
   */
  void execute(ThreadSaveExecution execution) throws Exception;
  
}
