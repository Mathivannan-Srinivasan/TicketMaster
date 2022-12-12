package paxos;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IServer<T> extends Remote {
  Acceptor<T> getAcceptor() throws RemoteException;;
  void applyChanges(T operation) throws RemoteException;;
}
