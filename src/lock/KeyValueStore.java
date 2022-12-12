package lock;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KeyValueStore implements IKeyValueStore {
  private Set<String> lockedSeats;

  public KeyValueStore() {
    this.lockedSeats = new HashSet<>();
  }

  @Override
  public void put(String key) {
    if(contains(key))
      throw new IllegalArgumentException("Seat is already locked");
    lockedSeats.add(key);
  }

  @Override
  public void put(List<String> keys) {
    if(contains(keys))
      throw new IllegalArgumentException("Seat is already locked");

    lockedSeats.addAll(keys);
  }

  @Override
  public boolean contains(String key) {
    return lockedSeats.contains(key);
  }

  @Override
  public boolean contains(List<String> keys) {
    return lockedSeats.containsAll(keys);
  }

  @Override
  public void delete(String key) {
    lockedSeats.remove(key);
  }

  @Override
  public void delete(List<String> keys) {
    keys.forEach(lockedSeats::remove);
  }

  @Override
  public Set<String> asCopy() {
    return new HashSet<>(lockedSeats);
  }

  @Override
  public void update(Set<String> state) {
    lockedSeats = state;
  }
}
