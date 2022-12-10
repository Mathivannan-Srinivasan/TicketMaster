package lock;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ILockCoordinator extends Remote {
  boolean accept(LockOperation operation) throws RemoteException;
  void commit(int commiterPort, LockOperation operation) throws RemoteException;
}
