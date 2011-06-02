package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.InformationState;
import java.util.BitSet;

public class SEQ_StraightMV extends SelfConfirmingPricePrediction
{
	private static final long serialVersionUID = 100L;
	
	public SEQ_StraightMV(int index)
	{
		super(index);
	}
	
	
	public String getPPName()
	{
		return "OSSCPP_StraightMV";
	}
	
	public double[] bid(InformationState state)
	{
		BitSet holding = new BitSet();
		int[] winner = state.getCurrentBidWinning();
		double[] price = state.getCurrentBidPrice();
		int round = state.getRound();
		
		for (int i=0;i<round;i++)
		{
			if (winner[i] == this.index)
			{
				holding.set(i);
			}
		}
		
		double max_free_surplus = 0.0;
		double max_unavail_surplus = 0.0;
		
		for (int i=0;i<Math.pow(2, NUM_GOODS - round);i++)
		{
			double free_surplus = 0.0;
			double unavail_surplus = 0.0;
			
			double freeCost = 0.0;
			double unavailCost = 0.0;
			BitSet bs = (BitSet)holding.clone();
			String bits = Integer.toBinaryString(i);
			bits = new StringBuffer(bits).reverse().toString();
			for (int j=0;j<bits.length();j++)
			{
				char bitChar = bits.charAt(j);
				String bitStr = String.valueOf(bitChar);
				int bit = Integer.parseInt(bitStr);
				if (bit == 1) bs.set(round + j, true);
				else bs.set(round + j, false);
			}
			
			int value = typeDist.get(bs).intValue();
			
			for (int j=0;j<bs.length();j++)
			{
				if (bs.get(j)) 
				{
					if (j == round) unavailCost += Double.POSITIVE_INFINITY;
					else
					{
						if (holding.get(j)) 
						{
							freeCost += price[j];
							unavailCost += price[j];
						}
						else
						{
							freeCost += this.pricePrediction[j];
							unavailCost += this.pricePrediction[j];
						}
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
		}
		double margVal = max_free_surplus - max_unavail_surplus;
		margVal = (margVal > 0) ? margVal : 0;
		double[] bid = new double[NUM_GOODS];
		bid[round] = margVal;
		return bid;
	}

}
