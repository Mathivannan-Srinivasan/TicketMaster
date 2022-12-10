package paxos;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Coordinator<T> {
  private static int proposalCount = 1;
  private final String serviceName;
  private final List<Integer> serverPorts;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public Coordinator(String serviceName, List<Integer> serverPorts) {
    this.serviceName = serviceName;
    this.serverPorts = serverPorts;
  }


  public boolean accept(T operation) {
    List<Acceptor<T>> acceptors = new ArrayList<>();
    logger.info("Starting Accept operation!");
    for(int port : serverPorts) {
      try {
        Registry registry = LocateRegistry.getRegistry(port);
        IServer<T> server = (IServer<T>) registry.lookup(serviceName + port);
        acceptors.add(server.getAcceptor());
      } catch (Exception e) {
          logger.log(Level.SEVERE, "Unable to get key valuer server " + port + ". " + e.getMessage());
      }
    }

    Proposal<T> proposal = new Proposal<T>(proposalCount++, operation);
    int neededVotes = Math.floorDiv(acceptors.size(), 2);
    int prepCount = 0;
    logger.info("Acceptor Count: " + acceptors.size() + ". Needed votes: " + neededVotes);

    for(Acceptor<T> acceptor : acceptors) {
      Promise<T> promise = acceptor.onPrepare(proposal);
      if (promise != null && promise.isDone())
        prepCount++;
    }
    logger.info("Prep Count: " + prepCount);
    if(prepCount < neededVotes)
      return false;

    int acceptCount = 0;
    for(Acceptor<T> acceptor : acceptors) {
      if(acceptor.onAccept(proposal))
        acceptCount ++;
    }
    logger.info(". Accept count: " + acceptCount);
    return acceptCount >= neededVotes;
  }

  public void commit(int commiterPort, T operation) {
    for(int port : serverPorts) {
      try {
        if (port == commiterPort)
          continue;
        Registry registry = LocateRegistry.getRegistry(port);
        IServer<T> server = (IServer<T>) registry.lookup(serviceName + port);
        server.applyChanges(operation);
      } catch (Exception e) {
        logger.log(Level.SEVERE, "Unable to commit changes to server " + port + ". " + e.getMessage());
      }
    }
  }

//  public static void main(String[] args) {
//    if(args.length < 1)
//      throw new IllegalArgumentException("Missing port number");
//    List<Integer> serverPorts = new ArrayList<>();
//    int coordinatorPort = Integer.parseInt(args[0]);
//
//    for (int i = 1; i < args.length; i++) {
//      serverPorts.add(Integer.parseInt(args[i]));
//    }
//
//    try {
//      Coordinator coordinator = new Coordinator(serverPorts);
//      ICoordinator stub = (ICoordinator) UnicastRemoteObject.exportObject(coordinator, 0);
//      Registry registry = LocateRegistry.getRegistry(coordinatorPort);
//
//      registry.bind("Coordinator", stub);
//
//    } catch (Exception e) {
//      logger.log(Level.SEVERE, "Unable to bind coordinator. " + e.getMessage());
//    }
//  }

}
