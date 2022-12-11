package datastore;

import java.io.Serializable;
import java.util.List;

public class BookingDetails implements Serializable {
  private final String id;
  private final String Name;
  private final String email;
  private final List<String> seats;

  public BookingDetails(String id, String name, String email, List<String> seats) {
    this.id = id;
    Name = name;
    this.email = email;
    this.seats = seats;
  }

  public String getId() {
    return id;
  }

  public String getName() {
    return Name;
  }

  public String getEmail() {
    return email;
  }

  public List<String> getSeats() {
    return seats;
  }
}
