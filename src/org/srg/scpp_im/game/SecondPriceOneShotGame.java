package org.srg.scpp_im.game;

import com.csvreader.*;
import org.srg.scpp_im.analysis.SCPPAnalyzer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.io.File;

/**
 * The base class for second price ons-shot auction games
 */
public class SecondPriceOneShotGame extends GameSetting implements Register {

	/** The strategies. */
	protected ArrayList<Strategy> strategies;
	
	/** The mode. */
	protected int mode;
	
	/** The simulation count. */
	protected int simulCount;
	
	/** The number of agents received. */
	protected int numAgentsReceived;
	
	/** The average price. */
	protected double[] avgPrice;
	
	/** The current prices of goods. */
	protected double[][] prices;
	
	/** The price prediction vector. */
	protected int[] pp;
	
	/** The distribution count. */
	protected int distCount;
	
	/** The sum of values obtained. */
	protected int[] sumValue;
	
	/** The payoff record. */
	protected double[][] payoffRecord;
	
	/** The average payoff. */
	protected double[] averagePayoff;
	
	/** The bit vector. */
	protected BitSet[] bitVector;
	
	/** The payoff_out. */
	protected PrintStream payoff_out;
	
	/** The path for price prediction vectors. */
	protected String pp_path = "pp_data/";
	
	/** The output path for price prediction vectors. */
	protected String pp_output_path = "pp_output/";
	
	/** The analyzer. */
	protected SCPPAnalyzer analyzer = null;
	
	/** The sum of optimal values. */
	protected double sumOptValue;
	//private boolean debug;
	
	/**
	 * Instantiates a new second price one-shot auction game.
	 */
	public SecondPriceOneShotGame()
	{
		numAgentsReceived = 0;
		sumOptValue = 0;
		simulCount = 0;
		avgPrice = new double[NUM_GOODS];
		prices = new double[NUM_SIMULATION][NUM_GOODS];
		sumValue = new int[NUM_GOODS];
		payoffRecord = new double[NUM_AGENT][NUM_ITERATION];
		averagePayoff = new double[NUM_AGENT];
		distCount = 0;
		pp = new int[NUM_GOODS];
		strategies = new ArrayList<Strategy>();
		bitVector = new BitSet[(int)Math.pow(2,NUM_GOODS)];

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
	
	/**
	 * Instantiates a new second price one-shot auction game.
	 *
	 * @param mode the mode of the game (production/training)
	 */
	public SecondPriceOneShotGame(int mode)
	{
		/*
		numAgentsReceived = 0;
		avgPrice = new double[NUM_GOODS];
		sumValue = new int[NUM_GOODS];
		distCount = 0;
		pp = new int[NUM_GOODS];
		strategies = new ArrayList<Strategy>();
		bitVector = new BitSet[(int)Math.pow(2,NUM_GOODS)];

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
		}*/
		super();
		this.mode = mode;
		try
		{
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				OutputStream os = new FileOutputStream(new File(SIMUL_PATH + "/payoff_data"));
				payoff_out = new PrintStream(os);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/** 
	 * Register an agent for participation in the SPSB game. Once the correct
	 * number of agents is registered, the game commences.
	 * @see org.srg.scpp_im.game.Register#register(org.srg.scpp_im.game.Strategy)
	 */
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
				
				if (ENABLE_ANALYZER) analyzer = new SCPPAnalyzer();
				
				avgPrice = new double[NUM_GOODS];
				prices = new double[NUM_SIMULATION][NUM_GOODS];
				simulCount = 0;
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					this.initTypeDist();
					//this.initTest();
					//System.out.println(i + " th simulation running..");
					run();
				}
				if (j>2 && Math.abs(maxDists[j-1]) > Math.abs(maxDists[j-2]))
				{
					updatePricePrediction(false, j);
				}
				else updatePricePrediction(true, j);
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
	
	/**
	 * Gets the maximum distance between previous and current price prediction
	 * vectors.
	 *
	 * @return the maximum distance of price prediction vectors
	 */
	protected double getMaxDist()
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
	
	/**
	 * Gets the accuracy of price prediction.
	 *
	 * @return the accuracy of price prediction vector.
	 */
	protected double getAccDist()
	{
		//double max_dist = 0;
		Strategy s = strategies.get(0);
		double accDist = s.getMaxDist(prices);
		
		//if (Math.abs(accDist) > Math.abs(max_dist))
		{
			//max_dist = accDist;
		}
		return accDist;
	}
	
	/**
	 * Update price prediction.
	 *
	 * @param updateAll whether to update everyone's price prediction vector
	 * @param currentIt the number of iterations so far
	 */
	protected void updatePricePrediction(boolean updateAll, int currentIt)
	{
		for (Strategy s : strategies)
		{
			s.setNewPredictionAverage(currentIt);
		}
	}
	
	/**
	 * Initializes the agent's type distribution for complementary case.
	 */
	protected void initComplementTypeDist()
	{
		Random ran = new Random();
		double compleThreshold = 0.5;
		Iterator<Strategy> iter = strategies.iterator();
		
		while(iter.hasNext())
		{
			int[] singleVal = new int[NUM_GOODS];
			Strategy s = iter.next();
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			
			for (BitSet bs : bitVector)
			{
				int val = ran.nextInt(VALUE_UPPER_BOUND + 1);
				
				if (bs.cardinality() == 1)
				{
					singleVal[bs.nextSetBit(0)] = val;
					typeDist.put(bs, new Integer(val));
				}
				else if (bs.cardinality() == 0) typeDist.put(bs, new Integer(0));
			}
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				for (BitSet bs : bitVector)
				{
					int extra = ran.nextInt(VALUE_UPPER_BOUND/2+1);
					int val = 0;
					if (i>0 && bs.cardinality() == i+1)
					{
						//System.out.println(bs);
						for (int j=0;j<bs.length();j++)
						{
							if (bs.get(j)) val += singleVal[j];
						}
						int max = val;
						// Complementary case
						if (ran.nextDouble() > compleThreshold)
						{
							for (BitSet bs2 : bitVector)
							{
								if (bs2.cardinality() > 1 && bs2.cardinality() < i+1)
								{
									//System.out.println("bs2 : " + bs2);
									BitSet subset = (BitSet)bs2.clone();
									subset.and(bs);
									//System.out.println("subset : " + subset);
									int subsetVal = typeDist.get(subset).intValue();
									max = (subsetVal > max) ? subsetVal : max;
								}
							}
						}
						val = max;
						typeDist.put(bs, new Integer(val));
					}
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
					System.out.println(bs + " " + value + " " + bs.cardinality());
				}
			}
		}
	}
	
	/**
	 * Test method for type distribution of agents.
	 */
	protected void initTest()
	{
		double[] price = {7,5,3};
		
		int[] typeDistOne = {0,0,0,0,0,0,0,15};
		int[] typeDistTwo = {0,8,6,8,5,8,6,8};
		int[] typeDistThree = {0,10,8,10,6,10,8,10};
		/*
		int[] typeDistOne = {0,5,5,5,5,5,5,5};
		int[] typeDistTwo = {0,0,0,0,0,0,0,12};
		int[] typeDistThree = {0,0,0,0,0,0,0,14};
		*/
		Iterator<Strategy> iter = strategies.iterator();
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			s.setPricePrediction(price);
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			if (s.getIndex() == 1)
			{
				for (int i=0;i<Math.pow(2, NUM_GOODS);i++)
				{
					BitSet bs = bitVector[i];
					typeDist.put(bs, new Integer(typeDistOne[i]));
				}
			}
			if (s.getIndex() == 2)
			{
				for (int i=0;i<Math.pow(2, NUM_GOODS);i++)
				{
					BitSet bs = bitVector[i];
					typeDist.put(bs, new Integer(typeDistTwo[i]));
				}
			}
			if (s.getIndex() == 3)
			{
				for (int i=0;i<Math.pow(2, NUM_GOODS);i++)
				{
					BitSet bs = bitVector[i];
					typeDist.put(bs, new Integer(typeDistThree[i]));
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
					System.out.println(bs + " " + value + " " + bs.cardinality());
				}
			}
		}
	}
	
	/**
	 * Initializes the type distribution.
	 */
	private void initTypeDist()
	{
		Random ran = new Random();
		// single-unit demand for agent_1
		int v_one = 3 + ran.nextInt(VALUE_UPPER_BOUND - 3);
		int v_i_upper_bound = ((NUM_GOODS * (v_one-1)) < VALUE_UPPER_BOUND) ? (NUM_GOODS * (v_one-1)) : VALUE_UPPER_BOUND;
		//System.out.println(v_one + " " + v_i_upper_bound);
		Iterator<Strategy> iter = strategies.iterator();
		
		//double[] price = {3.5,0.4,0.4,0.4,0.4};
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			
			//s.setPricePrediction(price);
			
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			if (s.getIndex() == 1)
			{
				// Give type distribution for every possible set of goods
				for (BitSet bs : bitVector)
				{
					
					//System.out.println(bs);
					if (bs.cardinality() >= 1)
					{
						typeDist.put(bs, new Integer(v_one));
					}
					else typeDist.put(bs, new Integer(0));
				}
			}
			else
			{
				int v_i = v_one + 1 + ran.nextInt(v_i_upper_bound - v_one);
				// Give type distribution for every possible set of goods
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() == NUM_GOODS)
					{
						typeDist.put(bs, new Integer(v_i));
					}
					else typeDist.put(bs, new Integer(0));
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
	
	/**
	 * Run the game.
	 */
	protected void run()
	{
		//ArrayList<int[]> bids = new ArrayList<int[]>();
		double[][] bids = new double[NUM_GOODS][NUM_AGENT];
		double[] highestAvgBids = new double[NUM_GOODS];
		double[] highestBids = new double[NUM_GOODS];
		double[] finalPrice = new double[NUM_GOODS];
		int[] finalWinner = new int[NUM_GOODS];
		// testing a single SAA

		InformationState state = new InformationState(NUM_GOODS);

		for (int i=0;i<NUM_AGENT;i++)
		{
			Strategy s = strategies.get(i);
			double[] newbid = s.bid(state);
			
			if (PRINT_DEBUG) System.out.print("Agent " + s.getIndex() + " bids: ");
			for (int j=0;j<NUM_GOODS;j++)
			{
				if (PRINT_DEBUG) System.out.print(newbid[j] + " ");
				bids[j][i] = newbid[j];
			}
			if (PRINT_DEBUG) System.out.println();
		}
		
		if (ENABLE_ANALYZER) state.setBids(bids); // Passing bids information only necessary when analyzer is enabled.
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			double[] bidsForGood = new double[NUM_AGENT];
			bidsForGood = bids[i];
			double[] sortedBids = new double[NUM_AGENT];
			
			for (int j=0;j<NUM_AGENT;j++)
			{
				sortedBids[j] = bidsForGood[j];
			}
			
			Arrays.sort(sortedBids);
			
			if (PRINT_DEBUG) System.out.print("Bids for Item " + i + ": ");
			for (int j=0;j<NUM_AGENT;j++)
			{
				if (PRINT_DEBUG) System.out.print(bidsForGood[j] + " ");
			}
			if (PRINT_DEBUG) System.out.println();
			
			int count = 0;
			double secondPrice = sortedBids[NUM_AGENT-2];
			double topPrice = sortedBids[NUM_AGENT-1];
			
			for (int j=0;count != 2 && j<NUM_AGENT;j++)
			{
				if (bidsForGood[j] == topPrice) 
				{
					count++;
				}
			}
			
			if (PRINT_DEBUG) System.out.println ("Second Price = " + secondPrice);
			//if (count == 1) secondPrice--; // Everyone bids same price. We pick a winner randomly.
			
			ArrayList<Integer> winners = new ArrayList<Integer>();
			int winner = 0;
			
			for (int j=0;j<NUM_AGENT;j++)
			{
				if (bidsForGood[j] > 0 && bidsForGood[j] == topPrice)
				{
					winners.add(new Integer(strategies.get(j).getIndex()));
				}
				/*
				if (bidsForGood[j] > secondPrice && bidsForGood[j] > 0) // if no tie
					winners.add(new Integer(strategies.get(j).getIndex()));
				if (count > 1) // there exists a tie
				{
					if (bidsForGood[j] == secondPrice && bidsForGood[j] > 0)
					{
						winners.add(new Integer(strategies.get(j).getIndex()));
					}
				}
				*/
			}
			if (winners.size() > 1)
			{
				Random r = new Random();
				winner = winners.get(r.nextInt(winners.size())).intValue();
			}
			else if (winners.size() == 1) winner = winners.get(0).intValue();
			
			finalWinner[i] = winner;
			highestBids[i] = topPrice;
			highestAvgBids[i] = (double)((NUM_AGENT - 1) * topPrice + secondPrice) / (double)NUM_AGENT;
			finalPrice[i] = secondPrice;
		}
		//state.setHighestBid(highestBids);
		if (HIGHEST_BID_PREDICTION) 
		{
			state.setTopAvgPrice(highestAvgBids);
			state.setTopPrice(highestBids);
		}
		else state.setBidPrice(finalPrice);
		state.setBidWinning(finalWinner);
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
		double[] curSurplus = new double[NUM_AGENT];
		if (ENABLE_ANALYZER || PRINT_DEBUG)
		{
			for (int i=0;i<NUM_AGENT;i++)
			{
				Strategy s = strategies.get(i);
				//System.out.println("asdf");
				curSurplus[i] = s.getCurrentSurplus(state);
			}
		}
		if (ENABLE_ANALYZER) 
		{
			for (int i=0;i<NUM_AGENT;i++)
			{
				Strategy s = strategies.get(i);
				analyzer.addUtility(s.getIndex(), curSurplus[i]);
			}
			analyzer.addState(state);
		}
		//System.out.println("add OB");
		for (Strategy s : strategies)
		{
			s.addObservation(state);
		}
		
		double[] currentBids = HIGHEST_BID_PREDICTION ? state.getCurrentTopAvgPrice() : state.getCurrentBidPrice();
		int[] currentWinning = state.getCurrentBidWinning();
		for (int i=0;i<NUM_GOODS;i++)
		{
			prices[simulCount][i] = currentBids[i];
			avgPrice[i] += currentBids[i];
		}
		simulCount++;
		if (PRINT_DEBUG)
		{
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
				System.out.println(s.getIndex() + ": " + curSurplus[i]);
			}
			System.out.println();
		}
		//this.numAgentsReceived = 0;
		//strategies.clear();
	}
}
