package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;

public class OSSCDP_BidEvaluator extends
		SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_BidEvaluator(int index)
	{
		super(index);
	}
	
	// Override
	public String getPPName()
	{
		return "OSSCDP_TargetMU";
	}

	public double[] bid(InformationState s)
	{
		//double[] newBid = new double[NUM_GOODS];
		/*
		int[] singleGoodValue = new int[NUM_GOODS];
		int[] priceToBid = new int[NUM_GOODS];
		int noPredCount = 0;
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (BitSet bs : bitVector)
			{
				if (bs.cardinality() == 1 && bs.get(i))
				{
					singleGoodValue[i] = typeDist.get(bs).intValue();
				}
			}
		}
		
		// no prediction 
		if (!this.isPricePredicting)
		{
			for (int i=0;i<NUM_GOODS;i++)
			{
				priceToBid[i] = singleGoodValue[i];
			}
		}

		// Given type-distribution and current information state find the subset that gives highest surplus
		double max_surplus = 0;
		BitSet maxSet = new BitSet();
		
		if (!this.isPricePredicting) // No price prediction - baseline case
		{
			//if (PRINT_DEBUG) System.out.println("No PP");
			for (BitSet bs : bitVector)
			{
				int value = typeDist.get(bs).intValue();
				
				// We bid on a set with the highest value.
				if (value > max_surplus)
				{
					maxSet = bs;
					max_surplus = value;
				}
			}
			int extra = (maxSet.cardinality() > 1) ? (int)Math.floor(max_surplus / (double)maxSet.cardinality()) : 0;
			for (int i=0;i<NUM_GOODS;i++)
			{
				if (max_surplus > 0 && maxSet.get(i)) 
				{
					newBid[i] = priceToBid[i] + extra;
				}
				else newBid[i] = 0;
			}
		}
		else*/
		//{
		double[] newBid = sampleMV();
			/*for (int i=0;i<NUM_GOODS;i++)
			{
				newBid[i] = (int)Math.round(sampleBid[i]);
			}*/
		//}
		return newBid;
	}
	
	private double[] sampleMV()
	{
		double[][] scenarios = new double[NUM_SCENARIO][NUM_GOODS];
		//double[][] candidates = new double[NUM_CANDIDATE_BID][NUM_GOODS];
		//double[] sumPrice = new double[NUM_GOODS];
		//double[] average_price = new double[NUM_GOODS];
		//double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		
		// Sample E scenarios
		for (int e=0;e<NUM_SCENARIO;e++)
		{
			double dist_num;
			if (PRINT_DEBUG) System.out.print("Scenario " + e + " for agent " + this.getIndex() + ": ");
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPrediction[i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPrediction[i], dist_num);
				if (pos >= 0) 
				{
					while (cumulPrediction[i][pos] == cumulPrediction[i][pos-1])
					{
						pos--;
					}
					scenarios[e][i] = pos;
				}
				else
				{
					scenarios[e][i] = ((pos * -1) - 1);
				}
				if (PRINT_DEBUG) System.out.print(scenarios[e][i] + " ");
			}
			if (PRINT_DEBUG) System.out.println();
		}
		
		// consider K candidate bids
		double[] bestBid = new double[NUM_GOODS];
		double bestUtil = 0;
		for (int k=0;k<NUM_CANDIDATE_BID;k++)
		{
			double[] candidateBid = targetMU();
			if (PRINT_DEBUG) 
			{
				System.out.print("Agent " + this.getIndex() + "'s candidate bid " + k + ": ");
				for (int j=0;j<NUM_GOODS;j++)
				{
					System.out.print(candidateBid[j] + " ");
				}
				System.out.println();
			}
			
			//boolean[] productWon = new boolean[NUM_GOODS];
			double totalUtil = 0.0;
			double utility = 0.0;
			double cost;
			for (int e=0;e<NUM_SCENARIO;e++)
			{
				cost = 0.0;
				BitSet productWon = new BitSet();
				for (int i=0;i<NUM_GOODS;i++)
				{
					if (candidateBid[i] > scenarios[e][i]) 
					{
						productWon.set(i);
						cost += scenarios[e][i];
					}
				}
				double value = typeDist.get(productWon) != null ? typeDist.get(productWon).intValue() : 0;
				utility = value - cost;
				totalUtil += utility/(double)NUM_SCENARIO;
			}
			if (PRINT_DEBUG) System.out.println("Agent " + this.getIndex() + "'s candidate bid " + k + 
					" has total utility of " + totalUtil);
			if (totalUtil > bestUtil)
			{
				bestUtil = totalUtil;
				for (int i=0;i<NUM_GOODS;i++)
				{
					bestBid[i] = candidateBid[i];
				}
			}
		}
		if (PRINT_DEBUG)
		{
			System.out.print("Best bid is :");
			for (int j=0;j<NUM_GOODS;j++)
			{
				System.out.print(bestBid[j] + " ");
			}
			System.out.println();
		}
		return bestBid;
	}
	
	private double[] targetMU()
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
				//dist_num  = 1 + ran.nextInt(NUM_SIMULATION + VALUE_UPPER_BOUND);
				/*
				for (int p=0;p<VALUE_UPPER_BOUND+1;p++)
				{
					if (dist_num <= cumulPrediction[i][p])
					{
						sumPrice[i] += p;
						break;
					}
				}
				*/
			}
		}
		
		// Get the expectation over price distribution, i.e. sampled K scenarios
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
		}
		
		// TargetMV on the average scenario
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
			margVal = (margVal > 0) ? margVal : 0;
			mv[i] = margVal;
		}
		
		double max_surplus = 0;
		BitSet maxSet = new BitSet();
		for (BitSet bs : bitVector)
		{
			double value = (double)typeDist.get(bs).intValue();
			double cost = 0.0;
			for (int j=0;j<bs.length();j++)
			{
				if (bs.get(j)) cost += average_price[j];
			}
			value -= cost;
			if (value > max_surplus)
			{
				maxSet = bs;
				max_surplus = value;
			}
		}
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (max_surplus > 0 && maxSet.get(i)) 
			{
			}
			else mv[i] = 0; // reset bid for item to 0 if it is not a part of optimal solution.
		}
		
		return mv;
	}
}
