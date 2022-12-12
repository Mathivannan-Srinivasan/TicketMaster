package loadbalance;

import datastore.BookingDetails;
import datastore.IDataStoreManager;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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
    for (int i = 0; i < seats.size(); i++) {
      seats.set(i, theatre + seats.get(i));
    }
    return lockServer.lockSeats(seats);
  }

  @Override
  public String bookTicket(String name, String email, String theatre, List<String> seats)
      throws RemoteException, NotBoundException {
    IDataStoreManager dataServer = getRandomDataServer();
    String ticketNum = dataServer.bookSeats(name, email, theatre, seats);
    ILockServer lockServer = getRandomLockServer();
    for (int i = 0; i < seats.size(); i++) {
      seats.set(i, theatre + seats.get(i));
    }
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
    return (IDataStoreManager) reg.lookup("DataStoreManager" + dataPorts.get(num));
  }

  private ILockServer getRandomLockServer() throws RemoteException, NotBoundException {
    int num = random.nextInt(lockPorts.size());
    Registry reg = LocateRegistry.getRegistry(lockPorts.get(num));
    return (ILockServer) reg.lookup("LockServer" + lockPorts.get(num));
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
      LoadBalancer obj = new LoadBalancer();
      loadBalancer = (ILoadBalancer) UnicastRemoteObject.exportObject(obj, 0);
      LocateRegistry.createRegistry(port);
      reg = LocateRegistry.getRegistry(port);
      reg.bind("TicketMaster" + port, loadBalancer);
    } catch (Exception e) {
      System.out.println(e.getMessage());
    }
  }

  private static class AESEncryption {

    private static SecretKey key;

    static {
      try {
        key = generateKey();
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
    }

    private static byte[] vector = generateVector();

    private static byte[] encrypt(String plainText) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(vector);
      cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
      return cipher.doFinal(plainText.getBytes());
    }

    private static String decrypt(byte[] cipherText) throws Exception {
      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
      IvParameterSpec ivParameterSpec = new IvParameterSpec(vector);
      cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
      byte[] result = cipher.doFinal(cipherText);
      return new String(result);
    }

    public static byte[] generateVector() {
      byte[] vec = new byte[16];
      new SecureRandom().nextBytes(vec);
      return vec;
    }

    private static SecretKey generateKey() throws NoSuchAlgorithmException {
      KeyGenerator aes = KeyGenerator.getInstance("AES");
      aes.init(256, new SecureRandom());
      return aes.generateKey();
    }
  }

}
