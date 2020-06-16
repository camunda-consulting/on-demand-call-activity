package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.engine.delegate.AbstractDelegateExecution;

/**
 * Setters for variables should be used to modify data
 *
 * @author Falko Menge (Camunda)
 */
public class ThreadSaveExecution extends AbstractDelegateExecution {

  private static final long serialVersionUID = 1L;

  private final String businessKey;
  private final String id;
  private final String processInstanceId;
  private final String processDefinitionId;
  private final String currentActivityId;
  private final String activityInstanceId;
  private final String tenantId;
  private final RuntimeService runtimeService;

  public ThreadSaveExecution(final DelegateExecution execution) {
    super(execution.getVariables());
    businessKey = execution.getBusinessKey();
    id = execution.getId();
    processInstanceId = execution.getProcessInstanceId();
    processDefinitionId = execution.getProcessDefinitionId();
    currentActivityId = execution.getCurrentActivityId();
    activityInstanceId = execution.getActivityInstanceId();
    tenantId = execution.getTenantId();

    runtimeService = execution.getProcessEngineServices().getRuntimeService();
  }

  public void signal() {
    signal(null);
  }

  public void signal(final Object signalData) {
    runtimeService.signal(getId(), null, signalData, getVariables());
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getProcessBusinessKey() {
    return businessKey;
  }

  @Override
  public String getProcessInstanceId() {
    return processInstanceId;
  }

  @Override
  public String getProcessDefinitionId() {
    return processDefinitionId;
  }

  @Override
  public String getCurrentActivityId() {
    return currentActivityId;
  }

  @Override
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  @Override
  public String getTenantId() {
    return tenantId;
  }  

  // TODO
//  @Override
//  public String getParentId() {
//  }

  // TODO
//  @Override
//  public String getParentActivityInstanceId() {
//  }
  
  // TODO check other overrides in the base class, some might be possible in this context
  // maybe split base class in two layers
  
}
