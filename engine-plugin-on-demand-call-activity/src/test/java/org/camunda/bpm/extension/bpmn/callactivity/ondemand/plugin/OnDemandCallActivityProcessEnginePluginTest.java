package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.apache.ibatis.logging.LogFactory;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.mock.Mocks;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String PROCESS_DEFINITION_KEY_EXCEPTION = "engine-plugin-on-demand-call-activity-exception";

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Long sleepTime = 3000L;

    static {
        LogFactory.useSlf4jLogging(); // MyBatis
    }

    @Before
    public void setup() {
        rule = cleanUpAndCreateEngine("camunda_on_demand_call_activity_test.cfg.xml", "process.bpmn", "process_child.bpmn", "process_with_mapping.bpmn", "process_exception.bpmn");
        Mocks.register("childProcessProvider", new ChildProcessProvider());
        Mocks.register("loggerDelegate", new LoggerDelegate());
        init(rule.getProcessEngine());
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
    }

    @Test
    public void testWithoutCallActivity() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, withVariables("retProcess", false
                        , "doThrowException", false));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        Thread.sleep(sleepTime);
        assertThat(processInstance).isEnded();
        assertThat(processInstance).job().isNull();
    }

    @Test
    public void testWithoutCallActivityFail() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        withVariables("retProcess", false, "doThrowException", true));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        Thread.sleep(sleepTime);
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
    }

    @Test
    public void testWithoutCallActivityRetries() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY,
                        withVariables("retProcess", false, "doThrowException", true));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        //CHECK IF THE FIRST RETRY JOB WAS CREATED AND THEN EXECUTED
        Thread.sleep(sleepTime);
        assertThat(processInstance).isNotEnded();
        logger.info(job().toString());
        execute(job());

        assertThat(processInstance).hasPassed("EndEventProcessEnded");
        assertThat(processInstance).isEnded();
        HistoricProcessInstance historicChildProcessInstance = historyService().createHistoricProcessInstanceQuery()
          .superProcessInstanceId(processInstance.getId())
          .singleResult();
        assertNotNull(historicChildProcessInstance);
        HistoricProcessInstance historicChildProcessInstanceCockpit = historyService().createNativeHistoricProcessInstanceQuery()
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
          .singleResult();
        // FIXME these assertion might need an sql fix in the engine and some additional code
        //assertNotNull(historicChildProcessInstanceCockpit);
        //assertThat(processInstance).calledProcessInstance("process-child").isEnded();
    }

    @Test
    public void testOnDemandCallActivityWithInputOutputMapping() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY_WITH_INOUTMAPPING,
                        withVariables("retProcess", false,
                                "doThrowException", false,
                                "inputVar", "anInputVariable"));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        Thread.sleep(sleepTime);
        assertThat(processInstance).hasPassed("EndEventProcessEnded");
        assertThat(processInstance).isEnded();
        assertThat(processInstance).variables().containsEntry("inputVar", "anInputVariable");
        assertThat(processInstance).variables().containsEntry("input", "anInputVariable");
        assertThat(processInstance).variables().containsEntry("outputVar", "someValue");
        assertThat(processInstance).variables().containsEntry("output", "someValue");
    }
    
    // TODO: test all operations that should normally work with a call activity (see engine test suite)
    @Test
    public void testWithoutCallActivityBadUserRequestException() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY, withVariables("retProcess", false
                        , "doThrowException", false, "badUserRequestException", true));
        assertThat(processInstance).calledProcessInstance("process-child").isNull();
        Thread.sleep(6000L);
        assertThat(processInstance).isEnded();
        assertThat(processInstance).job().isNull();
    }
    
    @Test
    public void testWithoutCallActivityOptimisticLockingException() throws InterruptedException {
        ProcessInstance processInstance = processEngine().getRuntimeService()
                .startProcessInstanceByKey(PROCESS_DEFINITION_KEY_EXCEPTION, withVariables("retProcess", false
                        , "doThrowException", false, "optimisticLockingException", true));
        //assertThat(processInstance).calledProcessInstance("process-child").isNull();
        Thread.sleep(15000L);
        //assertThat(processInstance).isEnded();
        //assertThat(processInstance).job().isNull();
    }
}