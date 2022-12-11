package datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import paxos.IServer;

public interface IDataStore extends Remote, IServer<DataOperation> {
  List<String> getAvailableSeats() throws RemoteException;
  String bookSeats(BookingDetails bookingDetails) throws RemoteException;
  BookingDetails getBookingDetails(String bookingId) throws RemoteException;
  void deleteBooking(String bookingId) throws RemoteException;
}
