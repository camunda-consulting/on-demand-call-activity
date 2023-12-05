package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityUtil.getAsyncServiceCallVarName;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.bpmn.servicetask.asynchronous.ThreadSaveExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for child process providers to be used together with
 * {@link OnDemandCallActivityBehavior}.
 *
 * @author Falko Menge (Camunda)
 */
public abstract class AbstractChildProcessProvider {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  public AbstractChildProcessProvider() {
    super();
  }

  /**
   * This method's implementation decides whether a regular BPMN child process
   * or an asynchronous service task implementation should be invoked.
   *
   * @param execution
   * @return Process definition key of the child process to be invoked or null
   *         to indicate that {@link #execute(OnDemandCallActivityExecution)}
   *         should be invoked instead.
   */
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
      OnDemandCallActivityExecution onDemandCallActivityExecution = new OnDemandCallActivityExecution(execution);
      updateOnDemandCallActivityExecutionVariables(execution, onDemandCallActivityExecution);
      execute(onDemandCallActivityExecution);
      execution.setVariableLocal(getAsyncServiceCallVarName(execution), true);
      return null;
    } else {
      return childProcess;
    }
  }

    public abstract void updateOnDemandCallActivityExecutionVariables(DelegateExecution execution, OnDemandCallActivityExecution onDemandCallActivityExecution);

}