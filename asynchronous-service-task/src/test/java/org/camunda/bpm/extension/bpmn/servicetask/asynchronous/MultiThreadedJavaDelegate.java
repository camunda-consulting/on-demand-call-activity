package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.concurrent.CompletableFuture;

/**
 * Sample implementation of {@link AbstractAsynchronousServiceTask} to test and
 * illustrate usage.
 *
 * @author Falko Menge (Camunda)
 */
public class MultiThreadedJavaDelegate extends AbstractAsynchronousServiceTask {

  public static CompletableFuture<Void> runAsyncFuture; // only needed for faster unit tests

  @Override
  public void execute(ThreadSaveExecution execution) {
    // Schedule a lambda to run asynchronously in a separate thread
    // and outside any database transaction.
    // This way the database connection is not blocked during the execution of
    // the following code and larger thread pool sizes can be used to
    // parallelize execution of code with a long i/o wait time, e.g. REST calls
    runAsyncFuture = CompletableFuture.runAsync(() -> {

      // variables can be created and modified using
      execution.setVariable("foo", "bar");

      // Example how even a normal JavaDelegate can be invoked.
      // However, it will be limited to operations that are thread-safe.
      LoggerDelegate loggerDelegate = new LoggerDelegate();
      try {
        loggerDelegate.execute(execution);
        if (Boolean.TRUE.equals(execution.getVariable("triggerBpmnSignal"))) {
          execution.signalEventReceived("Signal_AsyncServiceTaskInvoked");
        }
        execution.setVariable("isSuccess", true);
      } catch (ExecutionRolledBackException e) {
        // TODO maybe undo any side effects
        return;
      } catch (Exception exception) {
        // you MUST catch any exception and handle it

        // TODO invoke any self-healing code that you may have

        // If needed, tell the process with a variable about the failure
        exception.printStackTrace();
        execution.setVariable("exceptionMessage", exception.getMessage());
        execution.setVariable("isSuccess", false);
      }
      
      // complete() must be called in a separate thread
      // Variables will be automatically submitted. 
      try {
        execution.complete();
      } catch (ExecutionRolledBackException e) {
        // TODO maybe undo any side effects
      } 
    });
  }

}
