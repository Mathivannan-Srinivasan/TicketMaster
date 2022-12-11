package lock;

import java.util.List;
import java.util.Set;

public interface IKeyValueStore {
  void put(String key);
  void put(List<String> keys);
  boolean contains(String key);
  boolean contains(List<String> keys);
  void delete(String key);
  void delete(List<String> keys);
  Set<String> asCopy();
  void update(Set<String> state);
}
