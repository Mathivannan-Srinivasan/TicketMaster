package paxos;

import java.io.Serializable;

public class Operation implements Serializable {

  public enum OpType {
    NONE, PUT, DELETE
  }

  private final OpType opType;
  private final String[] values;

  public Operation() {
    this.opType = OpType.NONE;
    this.values = new String[]{};
  }

  public Operation(OpType opType, String[] values) {
    this.opType = opType;
    this.values = values;
  }

  public OpType getOpType() {
    return opType;
  }

  public String[] getValues() {
    return values;
  }
}