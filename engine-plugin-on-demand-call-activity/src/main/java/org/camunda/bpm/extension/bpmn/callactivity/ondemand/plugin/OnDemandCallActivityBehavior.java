package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.util.OnDemandCallActivityUtil.getAsyncServiceCallVarName;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;
import org.camunda.bpm.engine.impl.bpmn.behavior.CallActivityBehavior;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEntity;
import org.camunda.bpm.engine.impl.pvm.delegate.ActivityExecution;
import org.camunda.bpm.engine.variable.VariableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>A BPMN Call Activity that allows to skip the invocation of a sub-process
 * and behaves like a wait state instead, which can be resolved by invoking
 * {@link RuntimeService#signal(String, Map)}.</p>
 *
 * <p>Must be used with an implementation of {@link AbstractChildProcessProvider}
 * as calledElement in BPMN, e.g.,
 * <code>${childProcessProvider.getChildProcessDefinitionKey(execution)}</code>.
 * </p>
 *
 * @author Falko Menge (Camunda)
 */
public class OnDemandCallActivityBehavior extends CallActivityBehavior {

    private Logger logger = LoggerFactory.getLogger(getClass());

    public OnDemandCallActivityBehavior(){
        super();
    }

    public OnDemandCallActivityBehavior(Expression expression){
        super(expression);
    }

    public OnDemandCallActivityBehavior(String className){
        super(className);
    }

    @Override
    protected void startInstance(ActivityExecution execution, VariableMap variables, String businessKey) {
        // try to start process with the provided process definition key
        try {
            super.startInstance(execution, variables, businessKey);
        } catch (DefinitionKeyNullValueException e) {
            // process definition key is null
            if (execution.hasVariableLocal(getAsyncServiceCallVarName(execution))) {
                // => no child process needed
                logger.debug("Ignoring processDefinitionKey=null. There is no demand for creating a child process.");
            } else {
                // unexpected null value that does not come from a child process provider 
                throw e;
            }
        }
    }

    @Override
    public void signal(ActivityExecution execution, String signalName, Object signalData) throws Exception {
        if (signalData instanceof Exception) {
            Exception exception = (Exception) signalData;
            createAsynchronousContinuationJob(execution, exception);
        } else {
            execution.setVariableLocal(getAsyncServiceCallVarName(execution), null);
            leave(execution);
        }
    }

    public static void createAsynchronousContinuationJob(DelegateExecution execution, Exception exception) {
        MessageEntity message = new MessageEntity();
        message.setProcessInstanceId(execution.getProcessInstanceId());
        message.setProcessDefinitionId(execution.getProcessDefinitionId());
        message.setExecutionId(execution.getId());
        message.setExclusive(true);
        message.setJobHandlerType(ScopelessAsyncContinuationJobHandler.TYPE);
        // FIXME: eigenen Job-Handler bauen, der PvmAtomicOperation.ACTIVITY_EXECUTE anstößt. Dann steigst du wieder am richtigen Punkt in die Ausführung ein.
        message.setExceptionMessage(exception.getMessage());
        message.setExceptionStacktrace(getExceptionStacktrace(exception));
        Context.getCommandContext().getJobManager().send(message);
    }

    public static String getExceptionStacktrace(Exception exception) {
        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return stringWriter.toString();
    }
}
