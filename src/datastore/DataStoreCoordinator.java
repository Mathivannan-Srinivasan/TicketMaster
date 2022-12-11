package datastore;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import paxos.Coordinator;

public class DataStoreCoordinator extends Coordinator<DataOperation> implements IDataStoreCoordinator {

  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DataStoreCoordinator(String serviceName, List<Integer> serverPorts) {
    super(serviceName, serverPorts);
  }

  public static void main(String[] args) {
    if(args.length < 1)
      throw new IllegalArgumentException("Missing port number");
    List<Integer> serverPorts = new ArrayList<>();
    int coordinatorPort = Integer.parseInt(args[0]);

    for (int i = 1; i < args.length; i++) {
      serverPorts.add(Integer.parseInt(args[i]));
    }

    try {
      DataStoreCoordinator coordinator = new DataStoreCoordinator("DataStoreServer", serverPorts);
      IDataStoreCoordinator stub = (IDataStoreCoordinator) UnicastRemoteObject.exportObject(coordinator, 0);
      Registry registry = LocateRegistry.getRegistry(coordinatorPort);

      registry.bind("DataStoreServerCoordinator"+coordinatorPort, stub);

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to bind Data store Coordinator. " + e.getMessage());
    }
  }
}
