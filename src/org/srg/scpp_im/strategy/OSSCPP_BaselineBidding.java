package org.srg.scpp_im.strategy;

import java.util.BitSet;

import org.srg.scpp_im.game.InformationState;

public class OSSCPP_BaselineBidding extends SelfConfirmingPricePrediction
{
	private static final long serialVersionUID = 100L;
	
	public OSSCPP_BaselineBidding(int index)
	{
		super(index);
	}
	
	// Override
	public String getPPName()
	{
		return "OSSCPP_StraightMV";
	}
	
	public double[] bid(InformationState s)
	{
		double[] newBid = new double[NUM_GOODS];
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
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (this.pricePrediction[i] == 0)// || this.isSingleUnitDemand)
			{
				noPredCount++;
			}
		}
		
		// no prediction 
		if (noPredCount == NUM_GOODS)
		{
			for (int i=0;i<NUM_GOODS;i++)
			{
				priceToBid[i] = singleGoodValue[i];
			}
		}

		// Given type-distribution and current information state find the subset that gives highest surplus
		double max_surplus = Double.MIN_VALUE;
		BitSet maxSet = new BitSet();
		
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
		double extra = (maxSet.cardinality() > 1) ? max_surplus / (double)maxSet.cardinality() : 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (max_surplus > 0 && maxSet.get(i)) 
			{
				newBid[i] = priceToBid[i] + extra;
			}
			else newBid[i] = 0;
		}
		
		return newBid;
	}
}
