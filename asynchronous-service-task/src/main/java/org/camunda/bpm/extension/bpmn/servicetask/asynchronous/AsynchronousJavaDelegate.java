package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.Map;

import org.camunda.bpm.engine.delegate.DelegateExecution;

public abstract class AsynchronousJavaDelegate {

  private ThreadSaveExecution threadSaveExecution;

  final public void execute(DelegateExecution execution) throws Exception {
    threadSaveExecution = new ThreadSaveExecution(execution);
    execute(threadSaveExecution);
  }

  /**
   * This method's implementation must call {@link #completeTask(Map)}
   * or {@link ThreadSaveExecution#signal(Map)}
   * in another thread and with enough delay to let the engine commit the TX
   */
  abstract public void execute(ThreadSaveExecution execution) throws Exception;
  
  final protected void completeTask(final Map<String, Object> variables) {
    threadSaveExecution.signal(variables);
  }

}
