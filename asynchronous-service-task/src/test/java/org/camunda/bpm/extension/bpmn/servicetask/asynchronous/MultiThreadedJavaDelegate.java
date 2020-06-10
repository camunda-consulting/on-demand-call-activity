package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import static org.camunda.bpm.extension.bpmn.servicetask.asynchronous.CompletableFutureJava8Compatibility.delayedExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MultiThreadedJavaDelegate extends AsynchronousJavaDelegate {

  @Override
  public void execute(ThreadSaveExecution execution) throws Exception {
    // TODO prepare REST request using input variables
    CompletableFuture.runAsync(() -> { // simulates the sending of a non-blocking REST request
      // the code inside this lambda runs in a separate thread
      // and outside any database transaction
      System.out.println("Hello");
      Map<String, Object> newVariables = new HashMap<>();
      newVariables.put("foo", "bar");
      //execution.signal(newVariables);
      completeTask(newVariables);
    }, delayedExecutor(250L, TimeUnit.MILLISECONDS));
  }

}
