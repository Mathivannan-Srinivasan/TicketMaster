package datastore;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataStoreManager implements IDataStoreManager {
  private final HashMap<String, List<IDataStore>> theatreToServer;
  private final Random rand;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DataStoreManager() {
    this.theatreToServer = new HashMap<>();
    this.rand = new Random();
  }

  private void addServer(String theatre, IDataStore dataStore) {
    if(!theatreToServer.containsKey(theatre))
      theatreToServer.put(theatre, new ArrayList<>());
    List<IDataStore> servers = theatreToServer.get(theatre);
    servers.add(dataStore);
  }

  @Override
  public List<String> getAvailableSeats(String theatre) throws RemoteException {
    if(!theatreToServer.containsKey(theatre))
      throw new IllegalArgumentException("The given theatre is not supported yet");
    List<IDataStore> servers = theatreToServer.get(theatre);
    int idx = rand.nextInt(servers.size());
    return servers.get(idx).getAvailableSeats();
  }

  @Override
  public String bookSeats(String theatre, String name, String email, List<String> seats)
      throws RemoteException {
    if(!theatreToServer.containsKey(theatre))
      throw new IllegalArgumentException("The given theatre is not supported yet");
    List<IDataStore> servers = theatreToServer.get(theatre);
    int idx = rand.nextInt(servers.size());
    String id = UUID.randomUUID().toString();
    BookingDetails details = new BookingDetails(id, name, email, seats);
    return servers.get(idx).bookSeats(details);
  }

  @Override
  public BookingDetails getBookingDetails(String theatre, String bookingId) throws RemoteException {
    if(!theatreToServer.containsKey(theatre))
      throw new IllegalArgumentException("The given theatre is not supported yet");
    List<IDataStore> servers = theatreToServer.get(theatre);
    int idx = rand.nextInt(servers.size());
    return servers.get(idx).getBookingDetails(bookingId);
  }

  @Override
  public void deleteBooking(String theatre, String bookingId) throws RemoteException {
    if(!theatreToServer.containsKey(theatre))
      throw new IllegalArgumentException("The given theatre is not supported yet");
    List<IDataStore> servers = theatreToServer.get(theatre);
    int idx = rand.nextInt(servers.size());
    servers.get(idx).deleteBooking(bookingId);
  }

  public static void main(String[] args) {
    if(args.length < 1)
      throw new IllegalArgumentException("Port number missing");
    int port = Integer.parseInt(args[0]);
    List<Integer> serverPorts = new ArrayList<>();
    if(args.length < 4)
      throw new IllegalArgumentException("Must have at least 3 servers to support 3 cities");
    for (int i = 1; i < args.length; i++) {
      serverPorts.add(Integer.parseInt(args[i]));
    }

    String[] theatres = new String[] {"AMC", "REG", "CIN"};
    int ind = 0;

    try {
      DataStoreManager manager = new DataStoreManager();

      for (int serverPort : serverPorts) {
        String theatre = theatres[ind];
        Registry registry = LocateRegistry.getRegistry(serverPort);
        IDataStore server = (IDataStore) registry.lookup("DataStoreServer"+serverPort);
        manager.addServer(theatre, server);
      }

      IDataStoreManager stub = (IDataStoreManager) UnicastRemoteObject.exportObject(manager, 0);

      Registry registry = LocateRegistry.getRegistry(port);
      registry.bind("DataStoreManager"+port, stub);

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to create server. " + e.getMessage());
    }
  }
}
