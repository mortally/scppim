package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.util.Map;
import java.util.BitSet;

public class OneShotSelfConfirmingPricePrediction extends SelfConfirmingPricePrediction {
	
	private static final long serialVersionUID = 100L;
	
	public OneShotSelfConfirmingPricePrediction(int index)
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
			if (this.pricePrediction[i] == 0 || this.isSingleUnitDemand)
			{
				priceToBid[i] = singleGoodValue[i];
				noPredCount++;
			}
			else
			{
				priceToBid[i] = (int)Math.round(((this.pricePrediction[i] > (double)singleGoodValue[i]) ? this.pricePrediction[i] : (double)singleGoodValue[i]));
			}
		}
		
		// Given type-distribution and current information state find the subset that gives highest surplus
		double max_surplus = Double.MIN_VALUE;
		BitSet maxSet = new BitSet();
		
		if (noPredCount == NUM_GOODS) // No price prediction - baseline case
		{
			if (PRINT_DEBUG) System.out.println("No PP");
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
		}
		else
		{
			for (BitSet bs : bitVector)
			{
				int value = typeDist.get(bs).intValue();
				
				double cost = 0.0;
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) cost += priceToBid[j];
				}
				double surplus = (double)value - cost; 	
				if (surplus > max_surplus)
				{
					max_surplus = surplus;
					maxSet = bs;
				}
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
		
		return newBid;
	}

}
