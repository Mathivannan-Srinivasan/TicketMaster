package lock;

import com.sun.org.apache.xpath.internal.operations.Bool;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import lock.LockOperation.LockOperationType;
import paxos.Acceptor;

public class LockServer implements ILockServer {
  private final IKeyValueStore keyValueStore;
  private final int port;
  private final Acceptor<LockOperation> acceptor;
  private final int coordinatorPort;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public LockServer(int port, int coordinatorPort, Acceptor<LockOperation> acceptor) {
    this.port = port;
    this.acceptor = acceptor;
    this.coordinatorPort = coordinatorPort;
    this.keyValueStore = new KeyValueStore();
  }

  @Override
  public Set<String> getCopy() throws RemoteException {
    return keyValueStore.asCopy();
  }

  @Override
  public Boolean lockSeats(List<String> seats) throws RemoteException {
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorPort);
      ILockCoordinator coordinator = (ILockCoordinator) registry.lookup("LockCoordinator");
      LockOperation operation = new LockOperation(LockOperationType.PUT, seats);
      boolean isAccepted = coordinator.accept(operation);
      if(isAccepted) {
        keyValueStore.put(seats);
        coordinator.commit(port, operation);
        return true;
      } else {
        return false;
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public void releaseLocks(List<String> seats) throws RemoteException {
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorPort);
      ILockCoordinator coordinator = (ILockCoordinator) registry.lookup("LockCoordinator");
      LockOperation operation = new LockOperation(LockOperationType.DELETE, seats);
      boolean isAccepted = coordinator.accept(operation);
      if(isAccepted) {
        keyValueStore.delete(seats);
        coordinator.commit(port, operation);
      } else {
        throw new IllegalArgumentException("Failed to reach consensus");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Acceptor<LockOperation> getAcceptor() {
    return acceptor;
  }

  @Override
  public void applyChanges(LockOperation operation) {
    switch (operation.operation) {
      case PUT:
        keyValueStore.put(operation.arguments); break;
      case DELETE:
        keyValueStore.delete(operation.arguments);break;
      case NONE:
      default:
    }
  }

  private boolean recoverState(int replicaPort) {
    try {
      Registry registry = LocateRegistry.getRegistry(replicaPort);
      ILockServer server = (ILockServer) registry.lookup("LockServer" + replicaPort);
      this.keyValueStore.update(server.getCopy());
      return true;
    }
    catch (Exception e) {
      logger.log(Level.SEVERE, e.getMessage());
    }
    return false;
  }

  public static void main(String[] args) {
    if(args.length < 1)
      throw new IllegalArgumentException("Port number missing");
    int port = Integer.parseInt(args[0]);
    String portAsString = args[0];
    if(args.length < 2)
      throw new IllegalArgumentException("Coordinator Port number missing");
    int coordinatorPort = Integer.parseInt(args[1]);

    List<Integer> recoveryPorts = new ArrayList<>();
    for (int i = 2; i < args.length; i++) {
      recoveryPorts.add(Integer.parseInt(args[i]));
    }

    try {
      Acceptor<LockOperation> acceptor = new Acceptor<LockOperation>(portAsString);
      LockServer server = new LockServer(port, coordinatorPort, acceptor);

      for(int recovery : recoveryPorts) {
        if(server.recoverState(recovery))
          break;
      }

      ILockServer stub = (ILockServer) UnicastRemoteObject.exportObject(server, 0);
      LocateRegistry.createRegistry(port);
      Registry registry = LocateRegistry.getRegistry(port);
      registry.bind("LockServer"+port, stub);

    }
    catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to create server. " + e.getMessage());
    }
  }
}
