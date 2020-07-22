package org.camunda.bpm.extension.engine.delegate;

import java.util.Map;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.model.bpmn.instance.FlowElement;

/**
 * Abstract DelegateExecution that can be used for as base for DelegateExecution
 * implementations for executing a JavaDelegate.
 * 
 * It contains all methods that have to be implemented with additional REST API
 * calls or wrapper classes that emulate the Camunda Java API. 
 *
 * @author Falko Menge (Camunda)
 */
public abstract class AbstractRemoteDelegateExecution extends AbstractDelegateExecution implements DelegateExecution {

  private static final long serialVersionUID = 1L;

  protected RepositoryService repositoryService;

  public AbstractRemoteDelegateExecution() {
    super();
  }

  public AbstractRemoteDelegateExecution(Map<String, ? extends Object> variables) {
    super(variables);
  }

  @Override
  public String getCurrentActivityName() {
    return getBpmnModelElementInstance().getName();
  }

  @Override
  public FlowElement getBpmnModelElementInstance() {
    return getBpmnModelInstance().getModelElementById(getCurrentActivityId());
  }

  // TODO get XML from file in classpath 
  // see BPMN Model API https://github.com/camunda/camunda-bpmn-model
//  @Override
//  public BpmnModelInstance getBpmnModelInstance() {
//    return repositoryService.getBpmnModelInstance(getProcessDefinitionId());
//  }

  // TODO return own wrapper that talks to REST API for selected operations
//  @Override
//  public ProcessEngineServices getProcessEngineServices() {
//  }

  // TODO return own wrapper that talks to REST API for selected operations
//  @Override
//  public ProcessEngine getProcessEngine() {
//  }

  @Override
  public String getEventName() {
    throw new UnsupportedOperationException("This DelegateExecution implementation is not meant to be used for ExecutionListeners");
    // TODO add support for ExecutionListeners
  }

  @Override
  public String getCurrentTransitionId() {
    throw new UnsupportedOperationException("This DelegateExecution implementation is not meant to be used for ExecutionListeners");
    // TODO add support for ExecutionListeners
  }

  // TODO via REST
//  @Override
//  public Incident createIncident(String incidentType, String configuration) {
//  }

  // TODO via REST
//  @Override
//  public Incident createIncident(String incidentType, String configuration, String message) {
//  }

  // TODO via REST
//  @Override
//  public void resolveIncident(String incidentId) {
//  }

  // TODO via REST API, if that is supported by Camunda one day
//  @Override
//  public void setProcessBusinessKey(String businessKey) {
//  }

}