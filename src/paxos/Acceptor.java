package paxos;

import java.io.Serializable;
import java.util.logging.Logger;

public class Acceptor<T> implements Serializable {
  private Proposal<T> lastProposal;
  private final String id;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public Acceptor(String id) {
    this.id = id;
    this.lastProposal = new Proposal<T>();
  }

  public Proposal<T> getLastProposal() {
    return lastProposal;
  }

  public String getId() {
    return id;
  }

  public Promise<T> onPrepare(Proposal<T> proposal) {
    if(Math.random() < 0.1) {
      return null;
    }
    logger.info("On Prepare: proposal id: " + proposal.getId());
    if (proposal.getId() > lastProposal.getId()) {
      lastProposal = proposal;
      return new Promise<T>(proposal, true);
    } else {
      return new Promise<T>(null, false);
    }
  }

  public boolean onAccept(Proposal<T> proposal) {
    if(Math.random() < 0.1) {
      return false;
    }

    return lastProposal.equals(proposal);
  }
}
