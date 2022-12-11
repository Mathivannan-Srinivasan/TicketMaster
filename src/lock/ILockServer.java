package lock;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import paxos.IServer;

public interface ILockServer extends Remote, IServer<LockOperation> {

  Set<String> getCopy() throws RemoteException;

  Boolean lockSeats(List<String> seats) throws RemoteException;

  void releaseLocks(List<String> seats) throws RemoteException;
}
