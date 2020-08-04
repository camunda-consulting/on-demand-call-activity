package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityUtil.getAsyncServiceCallVarName;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.bpmn.servicetask.asynchronous.ThreadSaveExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChildProcessProvider {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  public AbstractChildProcessProvider() {
    super();
  }

  public abstract String decideOnChildProcess(DelegateExecution execution);

  /**
   * <p>This method's implementation must call {@link ThreadSaveExecution#complete()}
   * in a separate thread and with enough delay to let the engine commit the TX.
   *
   * <p>Exceptions must either be caught by the implementation and reported back to the
   * BPMN process using process variables that, e.g., a Gateway can react to
   * or invoke {@link OnDemandCallActivityExecution#handleFailure(Exception)}.
   */
  public abstract void execute(OnDemandCallActivityExecution execution);
  
  /**
   * This method should be invoked by the calledElement expression of a Call Activity
   */
  public String getChildProcessDefinitionKey(DelegateExecution execution) throws Exception {
    logger.info("Running childProcessProvider...");
    
    String childProcess = decideOnChildProcess(execution);
    if (childProcess == null || childProcess.isEmpty()) {
      execute(new OnDemandCallActivityExecution(execution));
      execution.setVariableLocal(getAsyncServiceCallVarName(execution), true);
      return null;
    } else {
      return childProcess;
    }
  }

}