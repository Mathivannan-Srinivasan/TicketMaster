package loadbalance;

import datastore.IDataStoreManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lock.ILockServer;
import datastore.BookingDetails;

public class LoadBalancer implements ILoadBalancer {

  private static ArrayList<Integer> dataPorts;
  private static ArrayList<Integer> lockPorts;
  private static int port;
  private static Random random = new Random();

  @Override
  public List<String> getAvailableSeats(String theatre) {
    IDataStoreManager server;
    try {
      int num = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(num));
      server = (IDataStoreManager) reg.lookup("DataStoreServer" + num);
      return server.getAvailableSeats(theatre);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Boolean blockSeats(String theatre, List<String> seats) {
    Random random = new Random();
    IDataStoreManager dataServer;
    ILockServer lockServer;
    try {
      int dp = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(dp));
      dataServer = (IDataStoreManager) reg.lookup("DataStoreServer" + dp);
      List<String> availableSeats = dataServer.getAvailableSeats(theatre);
      if (!availableSeats.containsAll(seats)) {
        return false;
      }
      int lp = random.nextInt(lockPorts.size());
      reg = LocateRegistry.getRegistry(dataPorts.get(lp));
      lockServer = (ILockServer) reg.lookup("LockServer" + dp);
      return lockServer.lockSeats(seats);
    } catch (Exception e) {
      return false;
    }
  }

  @Override
  public String bookTicket(String name, String email, String theatre, List<String> seats) {
    IDataStoreManager dataServer;
    ILockServer lockServer;
    int lp;
    int dp;
    try {
      dp = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(dp));
      dataServer = (IDataStoreManager) reg.lookup("DataStoreServer" + dp);
      String ticketNum = dataServer.bookSeats(name, email, theatre, seats);
      lp = random.nextInt(lockPorts.size());
      reg = LocateRegistry.getRegistry(dataPorts.get(lp));
      lockServer = (ILockServer) reg.lookup("LockServer" + dp);
      lockServer.releaseLocks(seats);
      return ticketNum;
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public BookingDetails getTicketDetails(String theatre, String ticketNo) {
    IDataStoreManager server;
    try {
      int num = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(num));
      server = (IDataStoreManager) reg.lookup("DataStoreServer" + num);
      return server.getBookingDetails(theatre, ticketNo);
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Boolean deleteTicket(String theatre, String ticketNo) {
    IDataStoreManager server;
    try {
      int num = random.nextInt(dataPorts.size());
      Registry reg = LocateRegistry.getRegistry(dataPorts.get(num));
      server = (IDataStoreManager) reg.lookup("DataStoreServer" + num);
      server.deleteBooking(theatre, ticketNo);
      return true;
    } catch (Exception e) {
      return null;
    }
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Enter load balancer port.");
      System.exit(1);
    }
    port = Integer.parseInt(args[0]);
    for (int i = 1; i < args.length; i++) {
      dataPorts.add(Integer.parseInt(args[i]));
    }

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
