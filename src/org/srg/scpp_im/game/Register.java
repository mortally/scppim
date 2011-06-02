package org.srg.scpp_im.game;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.srg.scpp_im.game.Strategy;

public interface Register extends Remote {
	public void register(Strategy s) throws RemoteException;
}
