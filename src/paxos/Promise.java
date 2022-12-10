package paxos;

import java.io.Serializable;

public class Promise<T> implements Serializable {
  private final Proposal<T> proposal;
  private final boolean isDone;

  public Promise(Proposal<T> proposal, boolean isDone) {
    this.proposal = proposal;
    this.isDone = isDone;
  }

  public boolean isDone() {
    return isDone;
  }

  public Proposal<T> getProposal() {
    return proposal;
  }
}
