package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin.TestUtil.cleanUpAndCreateEngine;
import static org.camunda.bpm.engine.test.assertions.ProcessEngineTests.*;
import static org.junit.Assert.*;

/**
 * Test case starting an in-memory database-backed Process Engine.
 */
public class OnDemandCallActivityProcessEnginePluginTest {

    @Rule
    public ProcessEngineRule rule;

    private static final String PROCESS_DEFINITION_KEY = "engine-plugin-on-demand-call-activity";
    private static final String PROCESS_DEFINITION_KEY_WITH_INOUTMAPPING = "engine-plugin-on-demand-call-activity-with-mapping";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Before
    public void setup() {
        rule = cleanUpAndCreateEngine("camunda_on_demand_call_activity_test.cfg.xml", "process.bpmn", "process_child.bpmn", "process_with_mapping.bpmn", "process_with_normal_call_activity.bpmn", "process_child_async.bpmn");
        Mocks.register("childProcessProvider", new ChildProcessProvider());
        Mocks.register("loggerDelegate", new LoggerDelegate());
        init(rule.getProcessEngine());
        ChildProcessProvider.invocationCount = 0;
    }

    @Test
    public void testWithCallActivity() {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, withVariables("retProcess", true));
        // FIXME test with 7.14.0-alpha1
//        assertThat(processInstance).calledProcessInstance().hasPassed("CallLoggerTask").isEnded();
        assertEquals(2, historyService().createHistoricProcessInstanceQuery().count());
        assertEquals(2, historyService().createHistoricVariableInstanceQuery()
            .variableName("retProcess")
            .count());
        assertEquals(1, historyService().createHistoricVariableInstanceQuery()
            .variableName("variableSetByChildProcessProvider")
            .count());
        assertThat(processInstance).isEnded();
        assertEquals(1, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivity() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, withVariables("retProcess", false
                        , "doThrowException", false));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isEnded();
        assertThat(processInstance).job().isNull();
        assertEquals(1, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivityFail() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        withVariables("retProcess", false, "doThrowException", true));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        assertEquals(1, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivityRetries() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        withVariables("retProcess", false, "doThrowException", true));
        assertEquals(1, ChildProcessProvider.invocationCount);
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        //CHECK IF THE FIRST RETRY JOB WAS CREATED AND THEN EXECUTED
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        execute(job());

        assertThatChildProcessIsInHistory(processInstance, 1);
        
        assertThat(processInstance).hasPassed("EndEvent_InParentProcess");
        assertThat(processInstance).isEnded();
        // this can not work because queries runtime:
        // assertThat(processInstance).calledProcessInstance("process-child").isEnded();
        assertEquals(2, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivityRetriesAndNormalCallActivity() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey("engine-plugin-on-demand-call-activity_with_normal_call_activity",
                        withVariables("retProcess", false, "doThrowException", true));
        assertEquals(1, ChildProcessProvider.invocationCount);
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        //CHECK IF THE FIRST RETRY JOB WAS CREATED AND THEN EXECUTED
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        execute(job());

        assertThatChildProcessIsInHistory(processInstance, 2);

        assertThat(processInstance).isEnded();
        // this can not work because queries runtime:
        // assertThat(processInstance).calledProcessInstance("process-child").isEnded();
        assertEquals(2, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivityRetriesAsync() throws InterruptedException {
        // child process with asyncBefore=true on start event
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        withVariables("retProcess", false, "doThrowException", true, "Async", true));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        assertEquals(1, ChildProcessProvider.invocationCount);
        //CHECK IF THE FIRST RETRY JOB WAS CREATED AND THEN EXECUTED
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        execute(job());

        assertThat(processInstance).calledProcessInstance("process-child-async").isActive();

        assertThatChildProcessIsInHistory(processInstance, 1);
        
        execute(jobQuery().singleResult());
        
        assertThat(processInstance).hasPassed("EndEvent_InParentProcess");
        assertThat(processInstance).isEnded();
        // this can not work because queries runtime:
        // assertThat(processInstance).calledProcessInstance("process-child").isEnded();
        assertEquals(2, ChildProcessProvider.invocationCount);
    }

    @Test
    public void testWithoutCallActivityRetriesAndNormalCallActivityAsync() throws InterruptedException {
        // child process with asyncBefore=true on start event
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey("engine-plugin-on-demand-call-activity_with_normal_call_activity",
                        withVariables("retProcess", false, "doThrowException", true, "Async", true));
        assertEquals(1, ChildProcessProvider.invocationCount);
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        //CHECK IF THE FIRST RETRY JOB WAS CREATED AND THEN EXECUTED
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        execute(job());

        assertThat(processInstance).calledProcessInstance("process-child-async").isActive();

        assertThatChildProcessIsInHistory(processInstance, 1);

        execute(jobQuery().singleResult());
        assertEquals(2, ChildProcessProvider.invocationCount);

        assertThatChildProcessIsInHistory(processInstance, 2);

        assertThat(processInstance).isEnded();
        // this can not work because queries runtime:
        // assertThat(processInstance).calledProcessInstance("process-child").isEnded();
        assertEquals(2, ChildProcessProvider.invocationCount);
    }

    private void assertThatChildProcessIsInHistory(ProcessInstance processInstance, int numberOfChildren) {
      List<HistoricProcessInstance> historicProcessInstances = historyService().createHistoricProcessInstanceQuery().superProcessInstanceId(processInstance.getId()).list();
      List<HistoricActivityInstance> historicActivityInstances = historyService().createHistoricActivityInstanceQuery().activityType("callActivity").list();

      assertEquals(numberOfChildren, historicProcessInstances.size());
      for(HistoricProcessInstance historicProcessInstance_ : historicProcessInstances){
          System.out.println("Historic Child Instance: "+historicProcessInstance_.getId());
          System.out.println(historicProcessInstance_.toString());
      }

      for(HistoricActivityInstance historicActivityInstance : historicActivityInstances){
          System.out.println("Historic Call Activity: "+historicActivityInstance.getId());
          System.out.println(historicActivityInstance.getActivityId() + " called '" + historicActivityInstance.getCalledProcessInstanceId() + "'");
          assertNotNull(historicActivityInstance.getCalledProcessInstanceId());
      }

      // this query is used by the Cockpit REST resource 
       int numberOfChildrenInCockpit = historyService().createNativeHistoricProcessInstanceQuery()
              .sql("select RES.* from (\n" +
                      "      select RES.ID_, RES.START_TIME_, RES.END_TIME_, ACT.ID_ AS ACT_INST_ID_, ACT.ACT_ID_, PROC_.ID_ AS PROC_DEF_ID_, PROC_.KEY_, PROC_.NAME_, INCIDENT.INCIDENT_TYPE_, INCIDENT.INCIDENT_COUNT_\n" +
                      "      from\n" +
                      "        ACT_HI_PROCINST RES\n" +
                      "      inner join\n" +
                      "        ACT_HI_ACTINST ACT\n" +
                      "      on\n" +
                      "        ACT.CALL_PROC_INST_ID_ = RES.ID_\n" +
                      "      inner join\n" +
                      "        ACT_RE_PROCDEF PROC_\n" +
                      "      on\n" +
                      "        RES.PROC_DEF_ID_ = PROC_.ID_" +
                      "      left join\n" +
                      "      (\n" +
                      "        select\n" +
                      "          INCIDENT.PROC_INST_ID_, INCIDENT.INCIDENT_TYPE_, count(INCIDENT.ID_) INCIDENT_COUNT_\n" +
                      "        from\n" +
                      "          ACT_HI_INCIDENT INCIDENT\n" +
                      "        group by\n" +
                      "          INCIDENT.PROC_INST_ID_, INCIDENT.INCIDENT_TYPE_\n" +
                      "      ) INCIDENT\n" +
                      "      on\n" +
                      "        RES.PROC_INST_ID_ = INCIDENT.PROC_INST_ID_\n" +
                      "\n" +
                      "WHERE ACT.PROC_INST_ID_ = #{parentProcessInstanceId}" +
                      ") RES")
              .parameter("parentProcessInstanceId", processInstance.getId())
              .list()
              .size();
      assertEquals(numberOfChildren, numberOfChildrenInCockpit);
    }

    @Test
    public void testOnDemandCallActivityWithInputOutputMapping() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY_WITH_INOUTMAPPING,
                        withVariables("retProcess", false,
                                "doThrowException", false,
                                "inputVar", "anInputVariable"));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).hasPassed("EndEventProcessEnded");
        assertThat(processInstance).isEnded();
        assertThat(processInstance).variables().containsEntry("inputVar", "anInputVariable");
        assertThat(processInstance).variables().containsEntry("input", "anInputVariable");
        assertThat(processInstance).variables().containsEntry("outputVar", "someValue");
        assertThat(processInstance).variables().containsEntry("output", "someValue");
        assertEquals(1, ChildProcessProvider.invocationCount);
   }
    
	@Test
	public void testOnDemandCallActivityWithParentVariableAccess() throws InterruptedException {
		ProcessInstance processInstance = processEngine().getRuntimeService().startProcessInstanceByKey(
				PROCESS_DEFINITION_KEY,
				withVariables("retProcess", false, "doThrowException", false, "setParentVar", true));
		assertThat(processInstance).calledProcessInstance("process-child").isNull();
        ChildProcessProvider.runAsyncFuture.join();
		assertThat(processInstance).hasPassed("EndEvent_InParentProcess");
		assertThat(processInstance).isEnded();
		assertThat(processInstance).variables().containsEntry("parentVar", "aParentVar");
        assertEquals(1, ChildProcessProvider.invocationCount);
	}
    
    // TODO: test all operations that should normally work with a call activity (see engine test suite)
    
    @Test
    public void testUserTaskForComparison() {
      BpmnModelInstance modelInstance = Bpmn.createExecutableProcess("user-task-process")
          .startEvent()
          .userTask()
          .endEvent()
          .done();
      repositoryService().createDeployment()
        .addModelInstance("user-task-process.bpmn", modelInstance)
        .deploy();
      ProcessInstance processInstance = runtimeService().startProcessInstanceByKey("user-task-process");
      assertThat(processInstance).isActive();
      claim(task(), "falko");
      assertEquals(0, ChildProcessProvider.invocationCount);
    }
    
    @Test
    public void testWithoutCallActivityBadUserRequestException() {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, withVariables("retProcess", false
                        , "doThrowException", false));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        ChildProcessProvider.runAsyncFuture.join();
        assertThat(processInstance).isEnded();
        assertThat(processInstance).job().isNull();
        assertEquals(1, ChildProcessProvider.invocationCount);
    }
}