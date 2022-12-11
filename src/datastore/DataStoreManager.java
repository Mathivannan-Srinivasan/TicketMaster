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
  private final HashMap<String, List<IDataStore>> cityToServer;
  private final Random rand;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DataStoreManager() {
    this.cityToServer = new HashMap<>();
    this.rand = new Random();
  }

  private void addServer(String city, IDataStore dataStore) {
    if(!cityToServer.containsKey(city))
      cityToServer.put(city, new ArrayList<>());
    List<IDataStore> servers = cityToServer.get(city);
    servers.add(dataStore);
  }

  @Override
  public List<String> getAvailableSeats(String city) throws RemoteException {
    if(!cityToServer.containsKey(city))
      throw new IllegalArgumentException("The given city is not supported yet");
    List<IDataStore> servers = cityToServer.get(city);
    int idx = rand.nextInt(servers.size());
    return servers.get(idx).getAvailableSeats();
  }

  @Override
  public String bookSeats(String city, String name, String email, List<String> seats)
      throws RemoteException {
    if(!cityToServer.containsKey(city))
      throw new IllegalArgumentException("The given city is not supported yet");
    List<IDataStore> servers = cityToServer.get(city);
    int idx = rand.nextInt(servers.size());
    String id = UUID.randomUUID().toString();
    BookingDetails details = new BookingDetails(id, name, email, seats);
    return servers.get(idx).bookSeats(details);
  }

  @Override
  public BookingDetails getBookingDetails(String city, String bookingId) throws RemoteException {
    if(!cityToServer.containsKey(city))
      throw new IllegalArgumentException("The given city is not supported yet");
    List<IDataStore> servers = cityToServer.get(city);
    int idx = rand.nextInt(servers.size());
    return servers.get(idx).getBookingDetails(bookingId);
  }

  @Override
  public void deleteBooking(String city, String bookingId) throws RemoteException {
    if(!cityToServer.containsKey(city))
      throw new IllegalArgumentException("The given city is not supported yet");
    List<IDataStore> servers = cityToServer.get(city);
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

    String[] cities = new String[] {"Boston", "New York", "San Francisco"};
    int cityIdx = 0;

    try {
      DataStoreManager manager = new DataStoreManager();

      for (int serverPort : serverPorts) {
        String city = cities[cityIdx];
        Registry registry = LocateRegistry.getRegistry(serverPort);
        IDataStore server = (IDataStore) registry.lookup("DataStoreServer"+serverPort);
        manager.addServer(city, server);
      }

      IDataStoreManager stub = (IDataStoreManager) UnicastRemoteObject.exportObject(manager, 0);

      Registry registry = LocateRegistry.getRegistry(port);
      registry.bind("DataStoreManager"+port, stub);

    } catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to create server. " + e.getMessage());
    }
  }
}
