package lock;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import paxos.IServer;

public interface ILockServer extends Remote, IServer<LockOperation> {

  Boolean lockSeats(List<String> seats) throws RemoteException;

  void releaseLocks(List<String> seats) throws RemoteException;
}
