package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

public class Async {
  
  public AsynchronousServiceTask factory(AsynchronousJavaDelegate delegate) {
    return new AsynchronousServiceTask(delegate);
  }

}
