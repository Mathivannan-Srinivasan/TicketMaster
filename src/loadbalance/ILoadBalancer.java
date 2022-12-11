package loadbalance;

import datastore.BookingDetails;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ILoadBalancer extends Remote {

  List<String> getAvailableSeats(String theatre) throws RemoteException, NotBoundException;

  Boolean blockSeats(String theatre, List<String> seats) throws RemoteException, NotBoundException;

  String bookTicket(String name, String email, String theatre, List<String> seats)
      throws RemoteException, NotBoundException;

  BookingDetails getTicketDetails(String theatre, String ticketNo)
      throws RemoteException, NotBoundException;

  Boolean deleteTicket(String theatre, String ticketNo) throws RemoteException, NotBoundException;
}
