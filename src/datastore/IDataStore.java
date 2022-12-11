package datastore;

import com.sun.tools.javac.util.Pair;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import paxos.IServer;

public interface IDataStore extends Remote, IServer<DataOperation> {
  Pair<HashMap<String, Boolean>, HashMap<String, BookingDetails>> getCopy() throws RemoteException;
  List<String> getAvailableSeats() throws RemoteException;
  String bookSeats(BookingDetails bookingDetails) throws RemoteException;
  BookingDetails getBookingDetails(String bookingId) throws RemoteException;
  void deleteBooking(String bookingId) throws RemoteException;
}
