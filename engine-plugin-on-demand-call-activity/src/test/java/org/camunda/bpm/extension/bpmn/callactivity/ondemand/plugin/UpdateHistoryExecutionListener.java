package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.db.entitymanager.DbEntityManager;
import org.camunda.bpm.engine.impl.history.event.HistoricActivityInstanceEventEntity;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.engine.impl.history.handler.HistoryEventHandler;
import org.camunda.bpm.engine.impl.history.producer.HistoryEventProducer;
import org.camunda.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.HistoricActivityInstanceEntity;

public class UpdateHistoryExecutionListener implements ExecutionListener {

    @Override
    public void notify(DelegateExecution delegateExecution) throws Exception {
        ExecutionEntity superExecution = ((ExecutionEntity) delegateExecution).getSuperExecution();
        DbEntityManager dbEntityManager = Context.getCommandContext().getDbEntityManager();

        final HistoryEventHandler historyEventHandler = Context.getProcessEngineConfiguration()
                .getHistoryEventHandler();
        final HistoryEventProducer historyEventProducer = Context.getProcessEngineConfiguration()
                .getHistoryEventProducer();
        HistoryEvent activityInstanceUpdateEvt = historyEventProducer.createActivityInstanceUpdateEvt(superExecution);

        HistoricActivityInstanceEventEntity cachedHistoryEvent = (HistoricActivityInstanceEventEntity) dbEntityManager.getCachedEntity(activityInstanceUpdateEvt.getClass(), activityInstanceUpdateEvt.getId());

        historyEventHandler.handleEvent(activityInstanceUpdateEvt);

        //dbEntityManager.forceUpdate(cachedHistoryEvent);

    }
}
