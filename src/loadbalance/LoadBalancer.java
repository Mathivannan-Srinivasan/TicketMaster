package loadbalance;

import datastore.BookingDetails;
import datastore.IDataStoreManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lock.ILockServer;

public class LoadBalancer implements ILoadBalancer {

  private static ArrayList<Integer> dataPorts = new ArrayList<>();
  private static ArrayList<Integer> lockPorts = new ArrayList<>();
  private static final Random random = new Random();

  @Override
  public List<String> getAvailableSeats(String theatre) throws RemoteException, NotBoundException {
    IDataStoreManager server = getRandomDataServer();
    return server.getAvailableSeats(theatre);
  }

  @Override
  public Boolean blockSeats(String theatre, List<String> seats)
      throws RemoteException, NotBoundException {
    IDataStoreManager dataServer = getRandomDataServer();
    List<String> availableSeats = dataServer.getAvailableSeats(theatre);
    if (!availableSeats.containsAll(seats)) {
      return false;
    }
    ILockServer lockServer = getRandomLockServer();
    return lockServer.lockSeats(seats);
  }

  @Override
  public String bookTicket(String name, String email, String theatre, List<String> seats)
      throws RemoteException, NotBoundException {
    IDataStoreManager dataServer = getRandomDataServer();
    String ticketNum = dataServer.bookSeats(name, email, theatre, seats);
    ILockServer lockServer = getRandomLockServer();
    lockServer.releaseLocks(seats);
    return ticketNum;
  }

  @Override
  public BookingDetails getTicketDetails(String theatre, String ticketNo)
      throws RemoteException, NotBoundException {
    IDataStoreManager server = getRandomDataServer();
    return server.getBookingDetails(theatre, ticketNo);
  }

  @Override
  public Boolean deleteTicket(String theatre, String ticketNo)
      throws RemoteException, NotBoundException {
    IDataStoreManager server = getRandomDataServer();
    server.deleteBooking(theatre, ticketNo);
    return true;
  }

  private IDataStoreManager getRandomDataServer() throws RemoteException, NotBoundException {
    int num = random.nextInt(dataPorts.size());
    Registry reg = LocateRegistry.getRegistry(dataPorts.get(num));
    return (IDataStoreManager) reg.lookup("DataStoreServer" + num);
  }

  private ILockServer getRandomLockServer() {
    try {
      int num = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(num));
      return (ILockServer) reg.lookup("LockServer" + num);
    } catch (Exception e) {
      return null;
    }
  }

  public static void main(String[] args) {
    if (args.length != 3) {
      System.out.println("Enter load balancer port.");
      System.exit(1);
    }
    int port = Integer.parseInt(args[0]);
    dataPorts.add(Integer.parseInt(args[1]));
    lockPorts.add(Integer.parseInt(args[2]));

    ILoadBalancer loadBalancer;
    Registry reg;

    try {
      ILoadBalancer obj = new LoadBalancer();
      loadBalancer = (LoadBalancer) UnicastRemoteObject.exportObject(obj, 0);
      LocateRegistry.createRegistry(port);
      reg = LocateRegistry.getRegistry(port);
      reg.bind("TicketMaster" + port, loadBalancer);
    } catch (Exception e) {
      System.exit(1);
    }
  }
}
