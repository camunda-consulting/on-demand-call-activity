package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.extension.bpmn.servicetask.asynchronous.ThreadSaveExecution;

/**
 * <p>An extended {@link ThreadSaveExecution} that allows to report failures back
 * to the {@link OnDemandCallActivityBehavior}. It can only be used in an
 * implementation of an {@link AbstractChildProcessProvider} for an On-demand
 * Call Activity.</p>
 *
 * <p>Inherits all capabilities of {@link ThreadSaveExecution}.</p>
 *
 * @author Falko Menge (Camunda)
 */
public class OnDemandCallActivityExecution extends ThreadSaveExecution implements DelegateExecution {

  private static final long serialVersionUID = 1L;

  private boolean transactionCommitted = false;

  /**
   * @param execution
   */
  public OnDemandCallActivityExecution(final DelegateExecution execution) {
    super(execution);
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, c -> {
      this.setTransactionCommitted(true);
    });
  }

  /**
   * Signals that the work that was tried asynchronously in a separate thread
   * could not be successfully executed.
   *
   * {@link OnDemandCallActivityBehavior#signal()} has an error handling that
   * gets triggered if an Exception is passed as signalData to
   * {@link RuntimeService#signal(String, String, Object, Map)}.
   *
   * @param exception the {@link Exception} that was caught by the thread.
   */
  public void handleFailure(final Exception exception) {
    getRuntimeService().signal(getId(), null, exception, getVariables());
  }

  public boolean isTransactionCommitted() {
    return transactionCommitted;
  }

  public void setTransactionCommitted(boolean transactionCommitted) {
    this.transactionCommitted = transactionCommitted;
  }

  @Override
  public RuntimeService getRuntimeService() {
    while (!isTransactionCommitted()) {
      // TODO handle TX rollback
      try {
        Thread.sleep(100); // TODO don't block the thread
      } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }
    return super.getRuntimeService();
  }
  
}
