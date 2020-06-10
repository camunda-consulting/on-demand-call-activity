package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.test;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityUtil.getAsyncServiceCallVarName;

public class NullActivityProcessProvider {

    public String getChildProcessDefinitionKey(DelegateExecution execution){
            execution.setVariableLocal(getAsyncServiceCallVarName(execution), true);
            return null;
    }
}
