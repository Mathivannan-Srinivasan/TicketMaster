package loadbalance;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import lock.ILockServer;

public class LoadBalancer implements ILoadBalancer {

  private static ArrayList<Integer> serverPorts;
  private static int port;

  @Override
  public List<String> getAvailableSeats(String theatre) {
    Random random = new Random();
    ILockServer server;
    try {
      int num = random.nextInt(serverPorts.size());
      Registry reg = LocateRegistry.getRegistry(serverPorts.get(num));
      server = (ILockServer) reg.lookup("LockServer" + num);
      // return server.getAvailableSeats(theatre);
    } catch (Exception e) {

    }
    return null;
  }

  @Override
  public Boolean blockSeats(List<String> seats) {
    Random random = new Random();
    ILockServer server;
    try {
      int num = random.nextInt(serverPorts.size());
      Registry reg = LocateRegistry.getRegistry(serverPorts.get(num));
      server = (ILockServer) reg.lookup("LockServer" + num);
      server.lockSeats(seats);
      return true;
    } catch (Exception e) {

    }
    return null;
  }

  @Override
  public String bookTicket(String name, String email, List<String> seats) {
    return null;
  }

  @Override
  public List<String> getTicketDetails(String ticketNo) {
    return null;
  }

  @Override
  public Boolean deleteTicket(String ticketNo) {
    return null;
  }

  public static void main(String[] args) {
    if (args.length == 0) {
      System.out.println("Enter load balancer port.");
      System.exit(1);
    }
    port = Integer.parseInt(args[0]);
    for (int i = 1; i < args.length; i++) {
      serverPorts.add(Integer.parseInt(args[i]));
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
