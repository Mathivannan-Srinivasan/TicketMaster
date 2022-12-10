package loadbalance;

import java.rmi.Remote;
import java.util.List;
import java.util.UUID;

public interface ILoadBalancer extends Remote {

  List<String> getAvailableSeats(String theatre);

  Boolean blockSeats(List<String> seats);

  UUID bookTicket(String name, String email);

  List<String> getTicketDetails(UUID ticketNo);

  Boolean deleteTicket(UUID ticketNo);
}
