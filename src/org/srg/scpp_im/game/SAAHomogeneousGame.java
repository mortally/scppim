package org.srg.scpp_im.game;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class SAAHomogeneousGame extends GameSetting implements Register {
	/*
	private static int NUM_AGENT = 3;
	private static int NUM_GOODS = 3;
	private static long NUM_SIMULATION = 1000000;
	private static int VALUE_UPPER_BOUND = 50;*/
	private ArrayList<Strategy> strategies;
	
	private int numAgentsReceived;
	private double[] avgPrice;
	private int[] pp;
	private int distCount;
	private int[] sumValue;
	private BitSet[] bitVector;
	private boolean debug;
	
	public SAAHomogeneousGame()
	{
		numAgentsReceived = 0;
		avgPrice = new double[NUM_GOODS];
		sumValue = new int[NUM_GOODS];
		distCount = 0;
		pp = new int[NUM_GOODS];
		strategies = new ArrayList<Strategy>();
		bitVector = new BitSet[(int)Math.pow(2,NUM_GOODS)];
		debug = false;
		for (int i=0;i<Math.pow(2, NUM_GOODS);i++)
		{
			BitSet bs = new BitSet();
			String bits = Integer.toBinaryString(i);
			bits = new StringBuffer(bits).reverse().toString();
			for (int j=0;j<bits.length();j++)
			{
				char bitChar = bits.charAt(j);
				String bitStr = String.valueOf(bitChar);
				int bit = Integer.parseInt(bitStr);
				if (bit == 1) bs.set(j, true);
				else bs.set(j, false);
			}
			bitVector[i] = bs;
		}
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
					initTypeDist();
					//System.out.println(i + " th simulation running..");
					run();
				}
				updatePricePrediction();
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
			System.out.println("Average Valuation");
			for (int j=0;j<NUM_GOODS;j++)
			{
				System.out.print((double)this.sumValue[j] / (double)this.distCount + " ");
			}
			System.out.println();
			System.exit(0);
		}
	}
	
	private double getMaxDist()
	{
		double max_dist = 0;
		for (Strategy s : strategies)
		{
			if (Math.abs(s.getMaxDist()) > Math.abs(max_dist))
			{
				max_dist = s.getMaxDist();
			}
		}
		return max_dist;
	}
	
	private void updatePricePrediction()
	{
		for (Strategy s : strategies)
		{
			s.setNewPrediction();
		}
	}
	
	private void initTypeDist()
	{
		Random ran = new Random();
		// single-unit demand for agent_1
		//System.out.println(v_one + " " + v_i_upper_bound);
		Iterator<Strategy> iter = strategies.iterator();
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int v_0 = ran.nextInt(VALUE_UPPER_BOUND+1);
			
			int[] margVal = new int[NUM_GOODS];
			int[] cumulativeVal = new int[NUM_GOODS];
			cumulativeVal[0] = v_0;
			margVal[0] = v_0;
			sumValue[0] += v_0;
			for (int i=1;i<NUM_GOODS;i++)
			{
				margVal[i] = ran.nextInt(margVal[i-1]+1);
				cumulativeVal[i] = cumulativeVal[i-1] + margVal[i];
				sumValue[i] = sumValue[i] + cumulativeVal[i];
			}
			
			// Give type distribution for every possible set of goods
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			for (BitSet bs : bitVector)
			{
				int units = bs.cardinality();
				if (units > 0) typeDist.put(bs, new Integer(cumulativeVal[units-1]));
				else typeDist.put(bs, new Integer(0));
			}
			distCount++;
			s.setTypeDist(typeDist);
			//initPricePrediction(s);
			//tempSts.add(s);
		} // end while
		//strategies = tempSts;
		
		if (this.debug)
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
					System.out.println(bs + " " + value + " " + bs.cardinality());
				}
			}
		}
	}
	private void initPricePrediction(Strategy s)
	{
		// initial prediction of strategies are zeros
		/*
		int[] newP = new int[NUM_GOODS];
		Random r = new Random();
		for (int i=0;i<NUM_GOODS;i++)
		{
			newP[i] = r.nextInt(47) + 3;
		}
		s.setPricePrediction(newP);*/
	}
	private void run()
	{
		//ArrayList<int[]> bids = new ArrayList<int[]>();
		double[][] bids = new double[NUM_AGENT][NUM_GOODS];
		// testing a single SAA
		boolean isQuiescent = false;

		InformationState state = new InformationState(NUM_GOODS);
		while (!isQuiescent)
		{
			// Supply current information state to users
			double[] currentBids = state.getCurrentBidPrice();
			int[] currentWinning = state.getCurrentBidWinning();

			for (int i=0;i<NUM_AGENT;i++)
			{
				Strategy s = strategies.get(i);
				double[] newbid = s.bid(state);
				
				//System.out.print("Agent " + s.getIndex() + " bids: ");
				for (int j=0;j<NUM_GOODS;j++)
				{
					//System.out.print(newbid[j] + " ");
					bids[i][j] = newbid[j];
				}
				//System.out.println();
			}
			isQuiescent = true;
			for (int i=0;i<NUM_GOODS;i++)
			{
				double max = currentBids[i];
				//int agentWon = 0;
				for (int j=0; j<NUM_AGENT;j++)
				{
					if (bids[j][i] > max)
					{
						max = bids[j][i];
						//agentWon = j;
					}
				}
				if (currentBids[i] < max)
				{
					currentBids[i] = max;
					ArrayList<Integer> winners = new ArrayList<Integer>();
					int winner = 0;
					for (int j=0;j<NUM_AGENT;j++)
					{
						if (bids[j][i] == max)
						{
							winners.add(new Integer(strategies.get(j).getIndex()));
						}
					}
					// Random tie-break
					if (winners.size() > 1)
					{
						Random r = new Random();
						winner = winners.get(r.nextInt(winners.size())).intValue();
					}
					else winner = winners.get(0).intValue();
					currentWinning[i] = winner;
					isQuiescent = false;
				}
			}
			state.setBidPrice(currentBids);
			state.setBidWinning(currentWinning);
			/*
			System.out.print("Current prices: ");
			for (int i=0;i<NUM_GOODS;i++)
			{
				System.out.print(currentBids[i] + " ");
			}
			System.out.println();
			
			System.out.print("Agent currently winning: ");
			for (int i=0;i<NUM_GOODS;i++)
			{
				System.out.print(currentWinning[i] + " ");
			}
			System.out.println();
			System.out.println();
			*/
		} // end while
		
		for (Strategy s : strategies)
		{
			s.addObservation(state);
		}
		
		double[] currentBids = state.getCurrentBidPrice();
		int[] currentWinning = state.getCurrentBidWinning();
		for (int i=0;i<NUM_GOODS;i++)
		{
			avgPrice[i] += currentBids[i];
		}
		/*
		System.out.print("Final prices: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentBids[i] + " ");
		}
		System.out.println();
		System.out.print("Agent who won the good: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentWinning[i] + " ");
		}
		System.out.println();
		System.out.println("Utility of each agent: ");
		for (int i=0;i<NUM_AGENT;i++)
		{
			Strategy s = strategies.get(i);
			System.out.println(s.getIndex() + ": " + s.getCurrentSurplus(state));
		}
		System.out.println();
		*/
		//this.numAgentsReceived = 0;
		//strategies.clear();
	}
}
