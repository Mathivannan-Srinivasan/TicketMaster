package paxos;

import java.rmi.Remote;

public interface IServer<T> extends Remote {
  Acceptor<T> getAcceptor();
  void applyChanges(T operation);
}
