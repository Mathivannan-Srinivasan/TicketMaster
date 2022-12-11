package lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LockOperation implements Serializable {
  public enum LockOperationType implements Serializable {
    NONE, PUT, DELETE
  }
  public LockOperationType operation;
  public List<String> arguments;

  public LockOperation() {
    operation = LockOperationType.NONE;
    arguments = new ArrayList<>();
  }

  public LockOperation(LockOperationType operation, List<String> arguments) {
    this.operation = operation;
    this.arguments = arguments;
  }
}
