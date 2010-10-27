package org.srg.scpp_im.game;

import java.rmi.registry.LocateRegistry;
import java.rmi.UnmarshalException;
import java.rmi.registry.Registry;
import org.srg.scpp_im.game.Register;
import org.srg.scpp_im.strategy.*;
import java.util.Random;
import java.lang.reflect.Constructor;

public class SAAGameClient {
	private static int NUM_GOODS = 3;
	private static int UPPER_BOUND = 50;
	public static void main(String[] args)
	{
	    if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }
        try {
        	String name = "SAAGame";
            Registry registry = LocateRegistry.getRegistry(args[0]);
            Register reg = (Register) registry.lookup(name);
            int index = Integer.parseInt(args[2]);
            if (index < 1)
            {
            	System.out.println("index must be > 0");
            	return;
            }
            Class strategyClass = Class.forName(args[1]);
            Constructor con = strategyClass.getConstructor(new Class[]{int.class});
            
            Strategy s = (Strategy)con.newInstance(index);
            //Random ran = new Random();
            /*
            int[] typeDist = new int[NUM_GOODS];
            typeDist[0] = 3 + ran.nextInt(UPPER_BOUND - 3);
            int v_i_upper_bound;
            v_i_upper_bound = ((NUM_GOODS * (typeDist[0]-1)) < UPPER_BOUND) ? (NUM_GOODS * (typeDist[0]-1)) : UPPER_BOUND; 
            for (int i=1;i<NUM_GOODS;i++)
            {
            	typeDist[i] = typeDist[0] + 1 + ran.nextInt(v_i_upper_bound - (typeDist[0]+1));
            }
            scpp.setTypeDist(typeDist);
            */
            reg.register(s);
            /*
            for (int i=0;i<typeDist.length;i++)
        	{
            	System.out.print(typeDist[i] + " ");
        	}
            System.out.println();
            */
            System.out.println("scpp registered");
            
        } catch (UnmarshalException e) {
        	System.exit(-1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
