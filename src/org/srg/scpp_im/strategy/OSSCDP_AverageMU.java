package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.util.Map;
import java.util.BitSet;
import java.util.Random;
import java.util.Arrays;

public class OSSCDP_AverageMU extends SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	//private static final int NUM_SAMPLE = 200;
	
	public OSSCDP_AverageMU(int index)
	{
		super(index);
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
		double[] sumMV = new double[NUM_GOODS];
		Random ran = new Random();
		for (int k=0;k<NUM_SAMPLE;k++)
		{
			double[] sample_price = new double[NUM_GOODS];
			double dist_num;
			// Sample a single scenario
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
					sample_price[i] = pos;
				}
				else
				{
					sample_price[i] = (pos * -1) - 1;
				}
				/*
				for (int p=0;p<VALUE_UPPER_BOUND+1;p++)
				{
					if (dist_num <= cumulPrediction[i][p])
					{
						sample_price[i] = p;
						break;
					}
				}
				*/
			}
			
			// StraightMV on the single scenario
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
								freeCost += sample_price[j];
								unavailCost += sample_price[j];
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
				sumMV[i] += margVal / (double)NUM_SAMPLE;
			}
		}
		return sumMV;
	}
}

			/*
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
							freeCost += sample_price[j];
							unavailCost += sample_price[j];
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
			sumMV += margVal;
		}
		return sumMV / (double)NUM_SAMPLE;*/
