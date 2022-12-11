package loadbalance;

import datastore.BookingDetails;
import java.rmi.Remote;
import java.util.List;

public interface ILoadBalancer extends Remote {

  List<String> getAvailableSeats(String theatre);

  Boolean blockSeats(String theatre, List<String> seats);

  String bookTicket(String name, String email, String theatre, List<String> seats);

  BookingDetails getTicketDetails(String theatre, String ticketNo);

  Boolean deleteTicket(String theatre, String ticketNo);
}
