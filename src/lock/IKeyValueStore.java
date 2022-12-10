package lock;

import java.util.List;

public interface IKeyValueStore {
  void put(String key);
  void put(List<String> keys);
  boolean contains(String key);
  boolean contains(List<String> keys);
  void delete(String key);
  void delete(List<String> keys);
}
