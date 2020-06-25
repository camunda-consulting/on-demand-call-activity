package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityUtil.getAsyncServiceCallVarName;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.bpmn.servicetask.asynchronous.AsynchronousJavaDelegate;
import org.camunda.bpm.extension.bpmn.servicetask.asynchronous.ThreadSaveExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractChildProcessProvider implements AsynchronousJavaDelegate {

  protected Logger logger = LoggerFactory.getLogger(getClass());

  public AbstractChildProcessProvider() {
    super();
  }

  public abstract String decideOnChildProcess(DelegateExecution execution);

  public abstract void execute(ThreadSaveExecution execution);

  public String getChildProcessDefinitionKey(DelegateExecution execution) throws Exception {
    logger.info("Running childProcessProvider...");
    
    String childProcess = decideOnChildProcess(execution);
    if (childProcess == null || childProcess.isEmpty()) {
      execute(new ThreadSaveExecution(execution));
      // TODO is this the right place to set this variable?
      execution.setVariableLocal(getAsyncServiceCallVarName(execution), true);
      return null;
    } else {
      return childProcess;
    }
  }

}