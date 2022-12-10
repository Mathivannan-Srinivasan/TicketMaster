package loadbalance;

import java.util.List;
import java.util.UUID;

public class LoadBalancer implements ILoadBalancer {

  @Override
  public List<String> getAvailableSeats(String theatre) {
    return null;
  }

  @Override
  public Boolean blockSeats(List<String> seats) {
    return null;
  }

  @Override
  public UUID bookTicket(String name, String email) {
    return null;
  }

  @Override
  public List<String> getTicketDetails(UUID ticketNo) {
    return null;
  }

  @Override
  public Boolean deleteTicket(UUID ticketNo) {
    return null;
  }


}
