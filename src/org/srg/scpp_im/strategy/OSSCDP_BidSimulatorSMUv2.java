package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;

public class OSSCDP_BidSimulatorSMUv2 extends
		SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	//private static final int NUM_CANDIDATE_BID = 8;
	
	public OSSCDP_BidSimulatorSMUv2(int index)
	{
		super(index);
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}

	public double[] bid(InformationState s)
	{
		double[] newBid = sampleMV();
		return newBid;
	}
	
	private double[] sampleMV()
	{
		// Simulate a game with NUM_AGENT bids
		int num_agent = NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL; 
		double[] bestBid = new double[NUM_GOODS];
		double[][] candidateBids = new double[num_agent][NUM_GOODS];
		double bestUtil = 0;
		int bestBidder = 0;
		for (int k=0;k<num_agent;k++)
		{
			candidateBids[k] = straightMU();
			if (PRINT_DEBUG)
			{
				System.out.println("Agent " + this.getIndex() + "'s candidate bids");
				for (int i=0;i<NUM_GOODS;i++)
				{
					System.out.print(candidateBids[k][i] + " ");
				}
				System.out.println();
			}
		}
		double[] firstPrice = new double[NUM_GOODS];
		double[] secondPrice = new double[NUM_GOODS];
		int[] winner = new int[NUM_GOODS];
		boolean[] ties = new boolean[NUM_GOODS];
		for (int i=0;i<NUM_GOODS;i++)
		{
			ties[i] = false;
			for (int k=0;k<num_agent;k++)
			{
				if (candidateBids[k][i] != 0 && candidateBids[k][i] >= firstPrice[i])
				{
					if (candidateBids[k][i] == firstPrice[i]) ties[i] = true;
					else ties[i] = false;
					if (firstPrice[i] != 0) secondPrice[i] = firstPrice[i];
					firstPrice[i] = candidateBids[k][i];
					winner[i] = k;
				}
			}
		}
		
		Set<Entry<BitSet, Integer>> distSet = typeDist.entrySet();
		Iterator<Entry<BitSet, Integer>> it = distSet.iterator();
		
		BitSet bestSet = null;
		double cost, value, util, maxUtil;
		maxUtil = 0;
		while(it.hasNext())
		{
			cost = 0;
			Entry<BitSet, Integer> entry = it.next();
			BitSet bs = entry.getKey();
			value = entry.getValue();
			for (int i=0;i<NUM_GOODS;i++)
			{
				if (bs.get(i))
				{
					cost += secondPrice[i];
				}
			}
			util = value - cost;
			if (maxUtil < util)
			{
				maxUtil = util;
				bestSet = bs;
			}
		}
		double[] bid = new double[NUM_GOODS];
		
		if (bestSet != null)
		{
			for (int i=0;i<NUM_GOODS;i++)
			{
				if (bestSet.get(i))
				{
					bid[i] = firstPrice[i];
				}
			}
		}
		return bid;
	}
	
	private double[] straightMU()
	{
		double[] sumPrice = new double[NUM_GOODS];
		double[] average_price = new double[NUM_GOODS];
		double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		
		// Sample K scenarios
		for (int k=0;k<NUM_SAMPLE;k++)
		{
			double dist_num;
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPrediction[i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPrediction[i], dist_num);
				if (pos >= 0) 
				{
					// need to handle when there are multiple identical elements.
					// backtrack for identical elements
					while (cumulPrediction[i][pos] == cumulPrediction[i][pos-1])
					{
						pos--;
					}
					sumPrice[i] += pos;
				}
				else
				{
					sumPrice[i] += (pos * -1) - 1;
				}
			}
		}
		// Get the expectation over price distribution, i.e. sampled K scenarios
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
		}
		
		// StraightMV on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = 0;
			double max_unavail_surplus = 0;
			for (BitSet bs : bitVector)
			{
				double free_surplus = 0;
				double unavail_surplus = 0;
				int value = typeDist.get(bs).intValue();
				double freeCost = 0.0;
				double unavailCost = 0.0;
				
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) 
					{
						if (i==j) unavailCost += Double.POSITIVE_INFINITY;
						else
						{
							freeCost += average_price[j];
							unavailCost += average_price[j];
						}
					}
				}
				free_surplus = (double)value - freeCost;
				unavail_surplus = (double)value - unavailCost;
				if (free_surplus > max_free_surplus)
				{
					max_free_surplus = free_surplus;
				}
				if (unavail_surplus > max_unavail_surplus)
				{
					max_unavail_surplus = unavail_surplus;
				}
			} // end for
			
			double margVal = max_free_surplus - max_unavail_surplus;
			mv[i] = (margVal > 0) ? margVal : 0;
		}
		return mv;
	}
	
	private double[] straightMU(double[] average_price)
	{
	
		double[] mv = new double[NUM_GOODS];
		
		// StraightMV on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = 0;
			double max_unavail_surplus = 0;
			for (BitSet bs : bitVector)
			{
				double free_surplus = 0;
				double unavail_surplus = 0;
				int value = typeDist.get(bs).intValue();
				double freeCost = 0.0;
				double unavailCost = 0.0;
				
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) 
					{
						if (i==j) unavailCost += Double.POSITIVE_INFINITY;
						else
						{
							freeCost += average_price[j];
							unavailCost += average_price[j];
						}
					}
				}
				free_surplus = (double)value - freeCost;
				unavail_surplus = (double)value - unavailCost;
				if (free_surplus > max_free_surplus)
				{
					max_free_surplus = free_surplus;
				}
				if (unavail_surplus > max_unavail_surplus)
				{
					max_unavail_surplus = unavail_surplus;
				}
			} // end for
			
			double margVal = max_free_surplus - max_unavail_surplus;
			mv[i] = (margVal > 0) ? margVal : 0;
		}
		return mv;
	}
}
