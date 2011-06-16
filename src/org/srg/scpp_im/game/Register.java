package org.srg.scpp_im.game;

import java.rmi.Remote;
import java.rmi.RemoteException;
import org.srg.scpp_im.game.Strategy;

/**
 * The Interface Register for RMI.
 */
public interface Register extends Remote {
	
	/**
	 * Register a strategy to an game instance.
	 *
	 * @param s the strategy to be registered
	 * @throws RemoteException the remote exception
	 */
	public void register(Strategy s) throws RemoteException;
}
