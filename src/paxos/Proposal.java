package paxos;

import java.io.Serializable;

public class Proposal<T> implements Comparable<Proposal<T>>, Serializable {

  private final long id;
  private final T operation;

  public Proposal() {
    this.id = 0;
    this.operation = null;
  }

  public Proposal(long votes, T operation) {
    this.id = votes;
    this.operation = operation;
  }

  public long getId() {
    return id;
  }

  public T getOperation() {
    return operation;
  }

  @Override
  public int compareTo(Proposal o) {
    return Long.compare(id, o.id);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Proposal<T> proposal = (Proposal<T>) o;
    return id == proposal.id;
  }
}
