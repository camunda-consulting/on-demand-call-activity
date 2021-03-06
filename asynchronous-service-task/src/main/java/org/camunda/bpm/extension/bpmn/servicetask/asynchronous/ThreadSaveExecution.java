package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.impl.cfg.TransactionState;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.pvm.PvmException;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.extension.engine.delegate.DelegateExecutionDTO;

/**
 * <p>An implementation of {@link DelegateExecution} that can be safely passed to
 * another thread because it is disconnected from the engine and its entities.
 * It provides a {@link #complete()} method to signal back to the execution
 * that it was started with assuming that that is in a wait state.</p>
 *
 * <p>Setters for variables must be used to set new or update existing variables.
 * Removing variables and modifying local variables is not supported and the
 * methods for that have been marked as deprecated to indicate that.</p>
 *
 * <p>In general, any method of the {@link DelegateExecution} interface that does
 * not work in another thread will throw {@link UnsupportedOperationException}
 * when it is invoked at runtime. Please ensure full unit test coverage of any
 * code that uses this class to avoid any surprises at runtime.</p>
 *
 * <p>The {@link DelegateExecution} interface is implemented in order to provide a
 * familiar programming model and ensure feature coverage as good as possible
 * in multi-threaded environment. Nevertheless, the implementation of that
 * interface has to be considered best-effort and compiler safety is limited.</p>
 *
 * @author Falko Menge (Camunda)
 */
public class ThreadSaveExecution extends DelegateExecutionDTO implements DelegateExecution {

  private static final long serialVersionUID = 1L;

  private RuntimeService runtimeService;

  private final CompletableFuture<Void> executionCommitFuture = new CompletableFuture<Void>();
  private boolean transactionCommitted = false;

  
  public ThreadSaveExecution(final DelegateExecution execution) {
    super(execution);
    // Thorben said that RuntimeService is thread-safe
    runtimeService = execution.getProcessEngineServices().getRuntimeService();
    CommandContext commandContext = Context.getCommandContext();
    commandContext.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, c -> {
      executionCommitFuture.complete(null);
    });
    commandContext.getTransactionContext().addTransactionListener(TransactionState.ROLLED_BACK, c -> {
      executionCommitFuture.cancel(false);
    });
  }

  /**
   * Provides the {@link RuntimeService} once it is safe to use again on this
   * execution.
   *
   * This method blocks until the open transaction that created this execution
   * is committed to the database.
   *
   * @return {@link RuntimeService}
   */
  protected RuntimeService getRuntimeServiceWhenCommitted() {
    if (!isTransactionCommitted()) {
        try {
          executionCommitFuture.join();
        } catch (CancellationException e) {
          throw new ExecutionRolledBackException(e);
        }
        setTransactionCommitted(true);
    }
    return getRuntimeService();
  }

  /**
   * Complete the work that was done asynchronously in a separate thread by
   * invoking {@link RuntimeService#signal(String, Map)} using this execution's 
   * id and variables.
   *
   * The invocation of this method requires the execution to be in a wait state
   * and committed to the database.
   *
   * This method blocks until the open transaction that created this execution
   * is committed to the database.
   *
   * @throws ExecutionRolledBackException
   *          if the transaction that created this execution has been rolled back.
   */
  public void complete() {
    try {
      getRuntimeServiceWhenCommitted().signal(getId(), getVariables());
    } catch (PvmException e) {
      if (e.getMessage().equals("cannot signal execution " + getId() + ": it has no current activity")) {
        Execution execution = getRuntimeService().createExecutionQuery()
          .activityId(getCurrentActivityId()) // FIXME test with multiple instances running in parallel; maybe getActivityInstanceId() was meant here
          .singleResult();
        getRuntimeServiceWhenCommitted().signal(execution.getId(), getVariables());
      } else {
        throw e;
      }
    }
  }

  /**
   * Triggers a BPMN Signal by invoking {@link RuntimeService#signalEventReceived(String)}.
   *
   * Notifies the process engine that a signal event of name 'signalName' has
   * been received. Delivers the signal to all executions waiting on
   * the signal and to all process definitions that can started by this signal. <p/>
   *
   * <strong>NOTE:</strong> Notification and instantiation happen synchronously.
   *
   * This method blocks until the open transaction that created this execution
   * is committed to the database.
   *
   * @param signalName
   *          the name of the signal event
   *
   * @throws AuthorizationException
   *          <li>if notify an execution and the user has no {@link Permissions#UPDATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          or no {@link Permissions#UPDATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>
   *          <li>if start a new process instance and the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.</li>

   * @throws ExecutionRolledBackException
   *          if the transaction that created this execution has been rolled back.
   */
  public void signalEventReceived(String signalName) {
    getRuntimeServiceWhenCommitted().signalEventReceived(signalName);
  }
  
  /**
   * Obtain the value of a variable in a given process instance or execution
   * using {@link RuntimeService#getVariable(String, String)}.
   *
   * Searching for the variable is done in all scopes that are visible to the given execution (including parent scopes).
   * Returns null when no variable value is found with the given name or when the value is set to null.
   *
   * This method blocks until the open transaction that created this execution
   * is committed to the database but only when accessing a variable from
   * the current execution or it's parent execution within the same process
   * instance. The target execution must be committed already. If the target
   * execution is part of an open transaction race conditions must be expected.
   *
   * @param executionId id of process instance or execution, cannot be null.
   * @param variableName name of variable, cannot be null.
   *
   * @return the variable value or null if the variable is undefined or the value of the variable is null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          when permission are missing. See {@link RuntimeService#getVariable(String, String)}.
   * @throws ExecutionRolledBackException
   *          if the transaction that created this execution has been rolled back.
   */
  public Object getVariableFromExecution(final String executionId, final String variableName) {
    // TODO write test case for this
    RuntimeService runtimeService;
    if (executionId == getId() || executionId == getParentId()) {
      runtimeService = getRuntimeServiceWhenCommitted();
    } else {
      runtimeService = getRuntimeService();
    }
    return runtimeService.getVariable(executionId, variableName);
  }

  /**
   * Update or create a variable for a given process instance or execution.  If the variable does not already exist
   * somewhere in the execution hierarchy (i.e. the specified execution or any ancestor),
   * it will be created in the process instance (which is the root execution).
   *
   * @param executionId id of process instance or execution to set variable in, cannot be null.
   * @param variableName name of variable to set, cannot be null.
   * @param value value to set. When null is passed, the variable is not removed,
   * only it's value will be set to null.
   *
   * @throws ProcessEngineException
   *          when no execution is found for the given executionId.
   * @throws AuthorizationException
   *          when permission are missing. See {@link RuntimeService#setVariable(String, String, Object)}.
   */
  public void setVariableInExecution(final String executionId, final String variableName, Object value) {
    // TODO write test case for this
    getRuntimeServiceWhenCommitted().setVariable(executionId, variableName, value);
  }

  /**
   * Setting local variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void setVariableLocal(String variableName, Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Setting local variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void setVariablesLocal(Map<String, ? extends Object> variables) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariable(String variableName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariableLocal(String variableName) {
    throw new UnsupportedOperationException();
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariables() {
    throw new UnsupportedOperationException();
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariablesLocal() {
    throw new UnsupportedOperationException();
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariables(Collection<String> variableNames) {
    throw new UnsupportedOperationException();
  }

  public boolean isTransactionCommitted() {
    return transactionCommitted;
  }

  protected void setTransactionCommitted(boolean transactionCommitted) {
    this.transactionCommitted = transactionCommitted;
  }

  /**
   * Removing variables is not supported by
   * {@link RuntimeService#signal(String, Map)}.
   *
   * @deprecated as a hint that it will not work.
   */
  @Deprecated
  @Override
  public void removeVariablesLocal(Collection<String> variableNames) {
    throw new UnsupportedOperationException();
  }

  protected RuntimeService getRuntimeService() {
    return runtimeService;
  }
  
}
