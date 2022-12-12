package datastore;

import datastore.DataOperation.DataOperationType;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import paxos.Acceptor;

public class DataStore implements IDataStore {

  private HashMap<String, Boolean> seats;
  private HashMap<String, BookingDetails> bookings;
  private final int port;
  private final Acceptor<DataOperation> acceptor;
  private final int coordinatorPort;
  private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

  public DataStore(int totalSeats, int port, int coordinatorPort, Acceptor<DataOperation> acceptor) {
    this.seats = new HashMap<>();
    for (int i = 0; i < totalSeats; i++) {
      this.seats.put(""+ i+1, false);
    }
    this.bookings = new HashMap<>();
    this.port = port;
    this.acceptor = acceptor;
    this.coordinatorPort = coordinatorPort;
  }

//  @Override
//  public Pair<HashMap<String, Boolean>, HashMap<String, BookingDetails>> getCopy() throws RemoteException {
//    Pair<HashMap<String, Boolean>, HashMap<String, BookingDetails>> state = new Pair<>(
//        new HashMap<>(seats), new HashMap<>(bookings)
//    );
//    return state;
//  }

  @Override
  public List<String> getAvailableSeats() throws RemoteException {
    List<String> res = new ArrayList<>();
    seats.forEach((seatNumber, isBooked) -> {
      if(!isBooked) {
        res.add(seatNumber);
      }
    });
    return res;
  }

  private String _bookSeats(BookingDetails bookingDetails) {
    for (String seatNum : bookingDetails.getSeats()) {
      if (this.seats.get(seatNum))
        throw new IllegalArgumentException(String.format("Seat %s is already booked.", seatNum));
    }
    for (String seatNum : bookingDetails.getSeats()) {
      this.seats.put(seatNum, true);
    }
    this.bookings.put(bookingDetails.getId(), bookingDetails);
    return bookingDetails.getId();
  }

  @Override
  public String bookSeats(BookingDetails bookingDetails) throws RemoteException {
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorPort);
      IDataStoreCoordinator coordinator = (IDataStoreCoordinator) registry.lookup("DataStoreServerCoordinator"+coordinatorPort);
      DataOperation operation = new DataOperation(DataOperationType.BOOK, bookingDetails);
      boolean isAccepted = coordinator.accept(operation);
      if(isAccepted) {
        String bookingId = this._bookSeats(bookingDetails);
        coordinator.commit(port, operation);
        return bookingId;
      } else {
        throw new IllegalArgumentException("Failed to reach consensus");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public BookingDetails getBookingDetails(String bookingId) throws RemoteException {
    if(this.bookings.containsKey(bookingId))
      return this.bookings.get(bookingId);
    throw new IllegalArgumentException("Booking Id is not present");
  }

  private void _deleteBooking(BookingDetails details) {
    for (String seatNum : details.getSeats()) {
      this.seats.put(seatNum, false);
    }
    this.bookings.remove(details.getId());
  }

  @Override
  public void deleteBooking(String bookingId) throws RemoteException {
    if(!this.bookings.containsKey(bookingId))
      throw new IllegalArgumentException("Booking Id is not present");
    BookingDetails details = this.bookings.get(bookingId);
    try {
      Registry registry = LocateRegistry.getRegistry(coordinatorPort);
      IDataStoreCoordinator coordinator = (IDataStoreCoordinator) registry.lookup("DataCoordinator"+coordinatorPort);
      DataOperation operation = new DataOperation(DataOperationType.DELETE, details);
      boolean isAccepted = coordinator.accept(operation);
      if(isAccepted) {
        _deleteBooking(details);
        coordinator.commit(port, operation);
      } else {
        throw new IllegalArgumentException("Failed to reach consensus");
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  public Acceptor<DataOperation> getAcceptor() {
    return acceptor;
  }

  @Override
  public void applyChanges(DataOperation operation) {
    switch (operation.operation) {
      case BOOK:
        _bookSeats(operation.arguments);break;
      case DELETE:
        _deleteBooking(operation.arguments);break;
      case NONE:
      default:
    }
  }

//  private boolean recoverState(int replicaPort) {
//    try {
//      Registry registry = LocateRegistry.getRegistry(replicaPort);
//      IDataStore server = (IDataStore) registry.lookup("DataStoreServer" + replicaPort);
//      Pair<HashMap<String, Boolean>, HashMap<String, BookingDetails>> state = server.getCopy();
//      this.seats = state.fst;
//      this.bookings = state.snd;
//      return true;
//    }
//    catch (Exception e) {
//      logger.log(Level.SEVERE, e.getMessage());
//    }
//    return false;
//  }

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
      Acceptor<DataOperation> acceptor = new Acceptor<DataOperation>(portAsString);
      DataStore server = new DataStore(20, port, coordinatorPort, acceptor);

//      for(int recovery : recoveryPorts) {
//        if(server.recoverState(recovery))
//          break;
//      }

      IDataStore stub = (IDataStore) UnicastRemoteObject.exportObject(server, 0);
      LocateRegistry.createRegistry(port);
      Registry registry = LocateRegistry.getRegistry(port);
      registry.bind("DataStoreServer"+port, stub);

    }
    catch (Exception e) {
      logger.log(Level.SEVERE, "Unable to create server. " + e.getMessage());
    }
  }
}
