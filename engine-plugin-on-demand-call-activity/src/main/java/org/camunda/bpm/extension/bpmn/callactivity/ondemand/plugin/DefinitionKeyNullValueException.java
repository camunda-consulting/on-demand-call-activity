package org.camunda.bpm.extension.bpmn.callactivity.ondemand.plugin;

import org.camunda.bpm.engine.exception.NullValueException;

/**
 * @author Falko Menge
 */
public class DefinitionKeyNullValueException extends NullValueException {

  private static final long serialVersionUID = 1L;

  public DefinitionKeyNullValueException() {
    super();
  }

  public DefinitionKeyNullValueException(String message) {
    super(message);
  }

  public DefinitionKeyNullValueException(Throwable cause) {
    super(cause);
  }

  public DefinitionKeyNullValueException(String message, Throwable cause) {
    super(message, cause);
  }

}
