package org.srg.scpp_im.game;

import java.lang.reflect.Constructor;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * The experimental GameRunner class -- does not work.
 * @author Dong Young Yoon
 */
public class GameRunner {
	//SAAGameEngine engine = new SAAGameEngine();
	
	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String[] args)
	{
		if (System.getSecurityManager() == null) 
		{
		      System.setSecurityManager(new SecurityManager());
	    }
		
		try
		{
			String name = "SAAGame";
			Class serverClass = Class.forName(args[0]);
            Constructor con = serverClass.getConstructor(new Class[]{});
            
            Register s = (Register)con.newInstance();
			//SAAGameEngine engine = new SAAGameEngine();
			Register stub = (Register) UnicastRemoteObject.exportObject(s, 0);
			Registry reg = LocateRegistry.getRegistry();
			reg.rebind(name, stub);
			System.out.println("GameRunner bound");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
