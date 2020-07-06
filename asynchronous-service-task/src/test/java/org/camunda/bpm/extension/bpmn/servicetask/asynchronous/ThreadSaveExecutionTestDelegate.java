package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

public class ThreadSaveExecutionTestDelegate extends AbstractAsynchronousServiceTask {

  @Override
  public void execute(ThreadSaveExecution execution) {
    ThreadSaveExecutionTest.execution = execution;
  }

}
