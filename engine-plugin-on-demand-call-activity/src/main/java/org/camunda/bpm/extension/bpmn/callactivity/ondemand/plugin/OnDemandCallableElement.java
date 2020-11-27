/**
 * 
 */
package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.camunda.bpm.engine.delegate.VariableScope;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.core.model.BaseCallableElement;
import org.camunda.bpm.engine.impl.core.model.CallableElement;
import org.camunda.bpm.engine.impl.util.CallableElementUtil;

/**
 * An implementation of {@link CallableElement} that throws a
 * {@link NullValueException} as soon as possible when an empty definition key
 * is detected. It thereby avoids unnecessary database lookups by
 * {@link CallableElementUtil#getProcessDefinitionToCall(VariableScope, BaseCallableElement)}.
 *
 * @author Falko Menge (Camunda)
 */
public class OnDemandCallableElement extends CallableElement {

  /**
   * This constructor basically clones the given {@link CallableElement}.
   *
   * When updating to a new Camunda version, it MUST be verified that all
   * fields/properties of the super classes are correctly copied over.
   * 
   * @param callableElement
   */
  public OnDemandCallableElement(CallableElement callableElement) {
    super();
    setDefinitionKeyValueProvider(callableElement.getDefinitionKeyValueProvider());
    setBinding(callableElement.getBinding());
    setVersionValueProvider(callableElement.getVersionValueProvider());
    setVersionTagValueProvider(callableElement.getVersionTagValueProvider());
    setTenantIdProvider(callableElement.getTenantIdProvider());
    setDeploymentId(callableElement.getDeploymentId());
    setBusinessKeyValueProvider(callableElement.getBusinessKeyValueProvider());
    addInputs(callableElement.getInputs());
    addOutputs(callableElement.getOutputs());
    outputsLocal = (callableElement.getOutputsLocal());
  }
  
  @Override
  public String getDefinitionKey(VariableScope variableScope) {
    String definitionKey = super.getDefinitionKey(variableScope);
    if (definitionKey == null) {
      throw new DefinitionKeyNullValueException("The definition key of a callable element was null.");
    }
    return definitionKey;
  }

}
