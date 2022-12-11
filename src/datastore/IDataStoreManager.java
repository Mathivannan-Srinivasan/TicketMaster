package datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IDataStoreManager extends Remote {
  List<String> getAvailableSeats(String city) throws RemoteException;
  String bookSeats(String city, String name, String email, List<String> seats) throws RemoteException;
  BookingDetails getBookingDetails(String city, String bookingId) throws RemoteException;
  void deleteBooking(String city, String bookingId) throws RemoteException;
}
