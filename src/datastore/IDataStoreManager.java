package datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDataStoreManager extends Remote {
  List<String> getAvailableSeats(String theatre) throws RemoteException;
  String bookSeats(String name, String email, String theatre, List<String> seats) throws RemoteException;
  BookingDetails getBookingDetails(String theatre, String bookingId) throws RemoteException;
  void deleteBooking(String theatre, String bookingId) throws RemoteException;
}
