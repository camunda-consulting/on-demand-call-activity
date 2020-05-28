package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerDelegate implements JavaDelegate {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String processDefinitionId = execution.getProcessDefinitionId();

        RepositoryService repositoryService = Context.getProcessEngineConfiguration().getProcessEngine().getRepositoryService();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();

        logger.info("Running logger delegate for definition {} and instance id {}...", processDefinition.getKey(), execution.getProcessInstanceId());
    }
}
