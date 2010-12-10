package org.srg.scpp_im.game;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class SecondPriceOneShotMarketScheduling extends SecondPriceOneShotGame {

	public SecondPriceOneShotMarketScheduling()
	{
		super();
	}
	
	public void register(Strategy s)
	{
		if (numAgentsReceived == NUM_AGENT) return;
		// get Strategy object here
		
		// temporary setting of initial dist
		//this.initPricePrediction(s);
				
		strategies.add(s);
		System.out.println("Strategy added with index = " + s.getIndex());
		/*
		int[] typeDist = s.getTypeDist();
		for (int i=0;i<typeDist.length;i++)
    	{
        	System.out.print(typeDist[i] + " ");
    	}
        System.out.println();
        */
		numAgentsReceived++;
		if (numAgentsReceived == NUM_AGENT) 
		{
			double[][] avgPrices = new double[NUM_ITERATION][NUM_GOODS];
			double[] maxDists = new double[NUM_ITERATION];
			for (int j=0;j<NUM_ITERATION;j++)
			{
				double[] prevAvg = new double[NUM_GOODS];
				for (int k=0;k<avgPrice.length;k++)
				{
					prevAvg[k] = avgPrice[k];
				}
				//pp = prevAvg;
				
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}
				
				avgPrice = new double[NUM_GOODS];
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					//this.initTypeDistBayesian();
					this.initTypeDistShuffle();
					/*
					double[] pp = {14.5,17.0,10.7,8.45,4.33};
					for (Strategy st : strategies)
					{
						st.setPricePrediction(pp);
					}
					*/
					//this.initTest();
					//System.out.println(i + " th simulation running..");
					run();
				}
				if (j>2 && Math.abs(maxDists[j-1]) >= Math.abs(maxDists[j-2]))
				{
					updatePricePrediction(false);
				}
				else updatePricePrediction(true);
				/*
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}*/
				
				double max_dist = getMaxDist();
				/*
				for (int i=0;i<NUM_GOODS;i++)
				{
					avgPrice[i] = avgPrice[i]/(double)NUM_SIMULATION;
					if (Math.abs(avgPrice[i] - prevAvg[i]) > Math.abs(max_dist))
					{
						max_dist = avgPrice[i] - prevAvg[i];
					}
					//System.out.print(avgPrice[i] + " ");
				}*/
				//NumberFormat f = new DecimalFormat("###.#######");
				System.out.print("avg price after simulation: ");
				for (int i=0;i<NUM_GOODS;i++)
				{
					avgPrice[i] = avgPrice[i]/(double)NUM_SIMULATION;
					System.out.print(avgPrice[i] + " ");
					avgPrices[j][i] = avgPrice[i];
				}
				System.out.println();
				System.out.println(max_dist);
				maxDists[j] = max_dist;
			}
			for (int j=0;j<NUM_ITERATION;j++)
			{
				for (int i=0;i<NUM_GOODS;i++)
				{
					System.out.print(avgPrices[j][i] + " ");
				}
				System.out.println();
			}
			System.out.println();
			for (int j=0;j<NUM_ITERATION;j++)
			{
				System.out.println(maxDists[j]);
			}
			System.out.println();
			/*
			System.out.println("Average Valuation");
			for (int j=0;j<NUM_GOODS;j++)
			{
				System.out.print((double)this.sumValue[j] / (double)this.distCount + " ");
			}
			System.out.println();
			*/
			System.exit(0);
		}
	}
	
	private void initTypeDistBayesian()
	{
		Random ran = new Random();
		Iterator<Strategy> iter = strategies.iterator();
		
		int[] num_slot_required = new int[NUM_AGENT];
		
		for (int i=0;i<NUM_AGENT;i++)
		{
			num_slot_required[i] = 1 + ran.nextInt(NUM_GOODS);
		}
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int idx = s.getIndex() - 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required[idx];i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required[idx];i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() >= num_slot_required[idx] && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required[idx]]));
					}
					else if (bs.cardinality() < num_slot_required[idx]) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
		if (PRINT_DEBUG)
		{
			iter = strategies.iterator();
			while (iter.hasNext())
			{
				Strategy s = iter.next();
				System.out.println("Agent " + s.getIndex() + "'s type dist:");
				if (s.isSingleUnitDemand()) System.out.println("Single-unit demand.");
				else System.out.println("Non single-unit demand.");
				Map<BitSet, Integer> m = s.getTypeDist();
				
				for (BitSet bs : bitVector)
				{
					int value = m.get(bs).intValue();
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
		}
	}
	
	private void initTypeDistShuffle()
	{
		Random ran = new Random();
		Iterator<Strategy> iter = strategies.iterator();
		
		int[] num_slot_required = new int[NUM_AGENT];
		
		for (int i=0;i<NUM_AGENT;i++)
		{
			num_slot_required[i] = i % NUM_GOODS + 1;
		}
		for (int i=0;i<NUM_AGENT;i++)
		{
			int ranPos = ran.nextInt(NUM_AGENT);
			
			int temp = num_slot_required[ranPos];
			num_slot_required[ranPos] = num_slot_required[i];
			num_slot_required[i] = temp;
		}
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int idx = s.getIndex() - 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required[idx];i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required[idx];i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() >= num_slot_required[idx] && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required[idx]]));
					}
					else if (bs.cardinality() < num_slot_required[idx]) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
		if (PRINT_DEBUG)
		{
			iter = strategies.iterator();
			while (iter.hasNext())
			{
				Strategy s = iter.next();
				System.out.println("Agent " + s.getIndex() + "'s type dist:");
				if (s.isSingleUnitDemand()) System.out.println("Single-unit demand.");
				else System.out.println("Non single-unit demand.");
				Map<BitSet, Integer> m = s.getTypeDist();
				
				for (BitSet bs : bitVector)
				{
					int value = m.get(bs).intValue();
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
		}
	}
	
	private void initTypeDist()
	{
		Random ran = new Random();
		Iterator<Strategy> iter = strategies.iterator();
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int num_slot_required = s.getIndex() % NUM_GOODS + 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required;i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required;i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() >= num_slot_required && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required]));
					}
					else if (bs.cardinality() < num_slot_required) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
		if (PRINT_DEBUG)
		{
			iter = strategies.iterator();
			while (iter.hasNext())
			{
				Strategy s = iter.next();
				System.out.println("Agent " + s.getIndex() + "'s type dist:");
				if (s.isSingleUnitDemand()) System.out.println("Single-unit demand.");
				else System.out.println("Non single-unit demand.");
				Map<BitSet, Integer> m = s.getTypeDist();
				
				for (BitSet bs : bitVector)
				{
					int value = m.get(bs).intValue();
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
		}
	}
	
}
