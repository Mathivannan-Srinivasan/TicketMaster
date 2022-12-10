package lock;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LockOperation implements Serializable {
  public enum Operation implements Serializable {
    NONE, PUT, DELETE
  }
  public Operation operation;
  public List<String> arguments;

  public LockOperation() {
    operation = Operation.NONE;
    arguments = new ArrayList<>();
  }

  public LockOperation(Operation operation, List<String> arguments) {
    this.operation = operation;
    this.arguments = arguments;
  }
}
