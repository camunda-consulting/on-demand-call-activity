package org.camunda.bpm.extension.bpmn.servicetask.asynchronous;

import java.util.Collection;
import java.util.Map;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.extension.engine.delegate.DelegateExecutionDTO;

/**
 * An implementation of {@link DelegateExecution} that can be safely passed to
 * another thread because it is disconnected from the engine and its entities.
 * It provides a {@link #complete()} method to signal back to the execution
 * that it was started with assuming that that is in a wait state.
 *
 * Setters for variables must be used to set new or update existing variables.
 * Removing variables and modifying local variables is not supported and the
 * methods for that have been marked as deprecated to indicate that.
 *
 * In general, any method of the {@link DelegateExecution} interface that does
 * not work in another thread will throw {@link UnsupportedOperationException}
 * when it is invoked at runtime. Please ensure full unit test coverage of any
 * code that uses this class to avoid any surprises at runtime.
 *
 * The {@link DelegateExecution} interface is implemented in order to provide a
 * familiar programming model and ensure feature coverage as good as possible
 * in multi-threaded environment. Nevertheless, the implementation of that
 * interface has to be considered best-effort and compiler safety is limited.
 *
 * @author Falko Menge (Camunda)
 */
public class ThreadSaveExecution extends DelegateExecutionDTO implements DelegateExecution {

  private static final long serialVersionUID = 1L;

  protected final RuntimeService runtimeService;
  
  public ThreadSaveExecution(final DelegateExecution execution) {
    super(execution);
    // Thorben said that RuntimeService is thread-safe
    runtimeService = execution.getProcessEngineServices().getRuntimeService();
  }

  /**
   * Complete the work that was done asynchronously in a separate thread by
   * invoking {@link RuntimeService#signal(String, Map)} using this execution's 
   * id and variables.
   *
   * The invocation of this method requires the execution to be in a wait state
   * and committed to the database.
   */
  public void complete() {
    // TODO catch and retry on exceptions indicating that the transaction was not yet committed 
    runtimeService.signal(getId(), getVariables());
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
    runtimeService.signal(getId(), null, exception, getVariables());
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
  
}
