package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;

public class OSSCDP_TargetMU extends SelfConfirmingDistributionPricePrediction {

	private static final long serialVersionUID = 100L;

	public OSSCDP_TargetMU(int index)
	{
		super(index);
	}
	
	
	public double[] bid(InformationState s)
	{
		//int[] newBid = new int[NUM_GOODS];
		//int[] singleGoodValue = new int[NUM_GOODS];
		//int[] priceToBid = new int[NUM_GOODS];
		//int noPredCount = 0;
		/*
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
		double max_surplus = Double.MIN_VALUE;
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
		/*
		for (int i=0;i<NUM_GOODS;i++)
		{
			newBid[i] = (int)Math.round(sampleBid[i]);
		}*/
		//}
		return newBid;
	}
	
	private double[] sampleMV()
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
					while (cumulPrediction[pos] == cumulPrediction[pos-1])
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
		if (PRINT_DEBUG) System.out.print("Average Sampled Prices for agent " + this.getIndex() + ": ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
			if (PRINT_DEBUG)
			{
				System.out.print(average_price[i] + " ");
			}
		}
		if (PRINT_DEBUG) System.out.println();
		
		// TargetMV on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = Double.MIN_VALUE;
			double max_unavail_surplus = Double.MIN_VALUE;
			for (BitSet bs : bitVector)
			{
				double free_surplus = Double.MIN_VALUE;
				double unavail_surplus = Double.MIN_VALUE;
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
		
		double max_surplus = Double.MIN_VALUE;
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
