package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util;

import org.camunda.bpm.engine.delegate.DelegateExecution;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityConstants.*;

public class OnDemandCallActivityUtil {

    public static String getRetriesVarName(DelegateExecution execution) {
        return RETRIES_VAR_BASE_NAME + execution.getCurrentActivityId();
    }

    public static String getAsyncServiceCallVarName(DelegateExecution execution) {
        return ASYNC_SERVICE_CALL_VAR_BASE_NAME + execution.getCurrentActivityId();
    }

    public static String getSkipVarName(DelegateExecution execution){
        return SKIP_VAR_BASE_NAME + execution.getCurrentActivityId();
    }
}
