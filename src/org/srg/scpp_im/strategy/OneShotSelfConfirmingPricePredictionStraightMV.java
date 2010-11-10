package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.util.Map;
import java.util.BitSet;

public class OneShotSelfConfirmingPricePredictionStraightMV extends SelfConfirmingPricePrediction {
	
	private static final long serialVersionUID = 100L;
	
	public OneShotSelfConfirmingPricePredictionStraightMV(int index)
	{
		super(index);
	}
	
	public int[] bid(InformationState s)
	{
		int[] newBid = new int[NUM_GOODS];
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
		
		if (noPredCount == NUM_GOODS) // No price prediction - baseline case
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
		else
		{
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
								freeCost += this.pricePrediction[j];
								unavailCost += this.pricePrediction[j];
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
				newBid[i] = (int)Math.round(margVal);
			}
		}
		
		return newBid;
	}

}
