package org.camunda.bpm.engine.impl.util;

import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.runtime.Execution;

import java.util.List;

public class ExecutionUtil {

    public static void printExecutionTree(Execution execution, RuntimeService runtimeService){
        printExecutionTree(execution, runtimeService,1L);
    }

    private static void printExecutionTree(Execution execution, RuntimeService runtimeService, Long level){
        ExecutionEntity executionEntity = (ExecutionEntity) execution;
        System.out.println(StringUtils.repeat("-", level.intValue()) + " " + executionEntity.toString() + " "
                + executionEntity.getActivityId() + " " + executionEntity.getActivityInstanceId());
        String sql = "SELECT * FROM ACT_RU_EXECUTION CHILD WHERE CHILD.PARENT_ID_ = '"+execution.getId()+"'";
        List<Execution> childExecutions  = runtimeService.createNativeExecutionQuery().sql(sql).list();

        for(Execution execution_ : childExecutions){
            printExecutionTree(execution_, runtimeService, level  + 1);
        }
    }
}
