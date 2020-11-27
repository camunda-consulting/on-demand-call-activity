package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.CompletableFutureJava8Compatibility.delayedExecutor;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sample implementation of {@link AbstractChildProcessProvider} to test and
 * illustrate usage.
 *
 * @author Falko Menge (Camunda)
 */
public class ChildProcessProvider extends AbstractChildProcessProvider {

    /**
     * If this method returns null execute will be invoked
     */
    @Override
    public String decideOnChildProcess(DelegateExecution execution) {
      Boolean retProcess = (Boolean) execution.getVariable("retProcess");
      // hand over to child process for error handling
      if (execution.hasVariable("firstTryHasFailed")) {
          if (execution.hasVariable("Async")) {
            return "process-child-async"; // child process with asyncBefore=true on start event
          }
        return "process-child"; // process definition key
        // maybe also another process for repair or self-healing
      }
      
      if (retProcess) {
          execution.setVariable("variableSetByChildProcessProvider", true);
          return "process-child"; // process definition key
//        } else if (isGlobalTaskSupported) {
//            return "global-service-task";
      // TODO implement plugin check that detects if the plugin is installed in the engine
//        } else if (!isOnDemandCallActivitySupported) {
//            return "process-child"; // maybe "fallback-process-with-single-service-task" but that wouldn't have error handling
      } else {
          return null;
      }
    }

    @Override
    public void execute(OnDemandCallActivityExecution execution) {


      // TODO handle exceptions during request creation? Only needed during reactive REST calls
      
      // Publish a task to a scheduled executor. This method returns after the task has
      // been put into the executor. The actual service implementation (lambda) will not yet
      // be invoked:
      CompletableFuture.runAsync(() -> { // simulates the sending of a non-blocking REST request
          // the code inside this lambda runs in a separate thread outside the TX
          // this will not work: execution.setVariable("foo", "bar");
          // THE EXECUTION IS NOT THREAD-SAFE
          try {
        	  Object setParentVar = execution.getVariableFromExecution(execution.getId(), "setParentVar");
        	  if (setParentVar != null && (Boolean) setParentVar) {
        		execution.setVariableInExecution(execution.getId(), "parentVar", "aParentVar");
        	  }
        	  
              Boolean doThrowException = (Boolean) execution.getVariable("doThrowException");
              logger.info("Do throw exception: "+doThrowException);
              if (doThrowException) {
                throw new Exception("Exception!");
              }
              Logger logger = LoggerFactory.getLogger(getClass());
              logger.info("Executing async block...");

              execution.setVariable("outputVar", "someValue");
              execution.complete();
          } catch (Exception exception) {
            exception.printStackTrace();
            execution.setVariable("firstTryHasFailed", true); // TODO make variable name more unique and delete after use
            execution.handleFailure(exception);
            // sketch for self healing
//                  try {
//                    // synchronously call self-healing µs
//                    if (isIgnore) {
//                      runtimeService.signal(executionId, newVariables);
//                    } else if (isRetry) {
//                      // rule with define number of retries and delay
//                      runtimeService.signal(executionId, null, e, null);                      
//                    } else {
//                      // fallout => incident without retries 
//                      e.printStackTrace();
//                      runtimeService.signal(executionId, null, e, null);
//                    }
//                  } catch (Exception selfHealingException) {
//                    runtimeService.signal(executionId, null, selfHealingException, null); // maybe indicate that delayed retry by Camunda is needed 
//                  }
          }
          //INCIDENT AND BPMN ERROR
      }, delayedExecutor(250L, TimeUnit.MILLISECONDS));

    }
}
