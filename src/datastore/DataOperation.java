package datastore;

import java.io.Serializable;
import lock.LockOperation.LockOperationType;

public class DataOperation {
  public enum DataOperationType implements Serializable {
    NONE, BOOK, DELETE
  }

  public DataOperationType operation;
  public BookingDetails arguments;

  public DataOperation() {
    operation = DataOperationType.NONE;
    arguments = null;
  }

  public DataOperation(DataOperationType operation, BookingDetails arguments) {
    this.operation = operation;
    this.arguments = arguments;
  }
}
