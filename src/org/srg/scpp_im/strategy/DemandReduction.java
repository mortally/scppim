package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.BitSet;

public class DemandReduction extends GameSetting implements Serializable {//, Strategy {

	private static final long serialVersionUID = 100L;
	private static final int KAPPA = 5;
	
	private int index;
	//private int kappa;
	private boolean isSingleUnitDemand;
	private Map<BitSet, Integer> typeDist;
	
	private BitSet[] bitVector;
	
	public DemandReduction(int index)
	{
		this.index = index;
		this.isSingleUnitDemand = true;
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
	
	public int getIndex()
	{
		return index;
	}
	public Map<BitSet, Integer> getTypeDist()
	{
		return typeDist;
	}
	public void setTypeDist(Map<BitSet, Integer> typeDist)
	{
		this.typeDist = typeDist;
		this.checkSingleDemand();
	}
	
	public int getCurrentSurplus(InformationState s)
	{
		double[] currentBid = s.getCurrentBidPrice();
		int[] currentWinning = s.getCurrentBidWinning();
		
		BitSet bs = new BitSet();
		int cost = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (currentWinning[i] == index)
			{
				bs.set(i);
				cost += currentBid[i];
			}
		}
		int value;
		value = typeDist.get(bs) != null ? typeDist.get(bs).intValue() : 0;
		return (value - cost);
	}
	public <T>void setPricePrediction(T pp)
	{
		return;
	}
	public void printPrediction()
	{
		return;
	}
	public void setNewPrediction()
	{
		return;
	}
	public void resetObservation()
	{
		return;
	}
	public double getMaxDist()
	{
		return 0.0;
	}
	public void addObservation(InformationState s)
	{
		return;
	}
	
	public double[] bid(InformationState s)
	{
		double[] newBid = new double[NUM_GOODS];
		int[] rank = new int[NUM_GOODS];
		double[] currentBid = s.getCurrentBidPrice();
		double[] sortedPrice = new double[NUM_GOODS];
		int[] currentWinning = s.getCurrentBidWinning();
		double[] currentPrediction = new double[NUM_GOODS];
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			sortedPrice[i] = currentBid[i];			
		}
		Arrays.sort(sortedPrice);
		int count = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (i!=0 && sortedPrice[i] > sortedPrice[i-1]) count++;
			for (int j=0;j<NUM_GOODS;j++)
			{
				if (currentBid[j] == sortedPrice[i])
				{
					rank[j] = count;
				}
			}
		}
		
		// Initially play SB
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (this.isSingleUnitDemand)
			{
				currentPrediction[i] = (currentWinning[i] == index) ? currentBid[i] : currentBid[i] + 1;
			}
			else
			{
				currentPrediction[i] = (currentWinning[i] == index) ? currentBid[i] + KAPPA * rank[i] : currentBid[i] + 1 + KAPPA * rank[i]; 	
			}
		}
		System.out.print("Agent " + index + "'s prediction : ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentPrediction[i] + " ");
		}
		System.out.println();
		
		double max_surplus = Double.MIN_VALUE;
		BitSet maxSet = new BitSet();
		for (BitSet bs : bitVector)
		{
			int value = typeDist.get(bs).intValue();
			
			double cost = 0.0;
			for (int j=0;j<bs.length();j++)
			{
				if (bs.get(j)) cost += currentPrediction[j];
			}
			double surplus = (double)value - cost;
			if (surplus > max_surplus)
			{
				max_surplus = surplus;
				maxSet = bs;
			}
		}
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (maxSet.get(i) && max_surplus > 0) 
			{
				if (currentWinning[i] == index)
				{
					newBid[i] = currentBid[i];
				}
				else 
				{
					if (currentBid[i] < VALUE_UPPER_BOUND)
						newBid[i] = currentBid[i] + 1;
					else newBid[i] = VALUE_UPPER_BOUND;
				}
			}
			else newBid[i] = 0;
		}
		return newBid;
	}
	public boolean isSingleUnitDemand()
	{
		return this.isSingleUnitDemand;
	}
	private void checkSingleDemand()
	{
		int[] singleValue = new int[NUM_GOODS];
		for (BitSet bs : bitVector)
		{
			if (bs.cardinality() == 1)
			{
				singleValue[bs.length()-1] = this.typeDist.get(bs).intValue();
			}
		}
		for (BitSet bs : bitVector)
		{
			if (bs.cardinality() > 1)
			{
				int value = this.typeDist.get(bs).intValue();
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j))
					{
						if (value > singleValue[j])
						{
							this.isSingleUnitDemand = false;
						}
					}
				}
			}
		}
	}
	
	
}
