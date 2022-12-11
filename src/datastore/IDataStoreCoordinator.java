package datastore;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IDataStoreCoordinator extends Remote {
  boolean accept(DataOperation operation) throws RemoteException;
  void commit(int commiterPort, DataOperation operation) throws RemoteException;
}
