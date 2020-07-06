package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import static org.camunda.bpm.extension.bpmn.servicetask.asynchronous.CompletableFutureJava8Compatibility.delayedExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MultiThreadedJavaDelegate extends AbstractAsynchronousServiceTask {

  @Override
  public void execute(ThreadSaveExecution execution) {
    // Schedule a lambda to run asynchronously in a separate thread
    // and outside any database transaction.
    // This way the database connection is not blocked during the execution of
    // the following code and larger thread pool sizes can be used to
    // parallelize execution of code with a long i/o wait time, e.g. REST calls
    CompletableFuture.runAsync(() -> {

      // variables can be created and modified using
      execution.setVariable("foo", "bar");

      // Example how even a normal JavaDelegate can be invoked.
      // However, it will be limited to operations that are thread-safe.
      LoggerDelegate loggerDelegate = new LoggerDelegate();
      try {
        loggerDelegate.execute(execution);
        execution.setVariable("isSuccess", true);
      } catch (Exception exception) {
        // you MUST catch any exception and handle it

        // TODO invoke any self-healing code that you may have

        // If needed, tell the process with a variable about the failure
        execution.setVariable("exceptionMessage", exception.getMessage());
        execution.setVariable("isSuccess", false);
      }
      
      // complete() must be called in a separate thread and
      // with enough delay to let the engine commit the TX, e.g. 250ms
      // Variables will be automatically submitted. 
      execution.complete();
    }, delayedExecutor(500L, TimeUnit.MILLISECONDS));
  }

}
