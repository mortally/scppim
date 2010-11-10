package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.util.Map;
import java.util.BitSet;

public class OneShotSelfConfirmingDistributionPricePrediction extends GameSetting implements Serializable, Strategy {

	private static final long serialVersionUID = 100L;
	private static final int BETA = 50;
	
	private int index;
	private boolean isSingleUnitDemand;
	private Map<BitSet, Integer> typeDist;
	private int[][] pricePrediction;
	private int[][] prevPrediction;
	private int[][] priceObservation;
	private int observationCount;
	private BitSet[] bitVector;
	
	public OneShotSelfConfirmingDistributionPricePrediction(int index)
	{
		this.index = index;
		this.observationCount = 0;
		this.isSingleUnitDemand = true;
		prevPrediction = new int[NUM_GOODS][BETA+1];
		pricePrediction = new int[NUM_GOODS][BETA+1];
		priceObservation = new int[NUM_GOODS][BETA+1];
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<BETA+1;j++)
			{
				prevPrediction[i][j] = 0;
				pricePrediction[i][j] = 0;
				priceObservation[i][j] = 0;
			}
		}
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
	public <T>void setPricePrediction(T pp)
	{
		this.pricePrediction = (int[][])pp;
	}
	
	public int getCurrentSurplus(InformationState s)
	{
		int[] currentBid = s.getCurrentBidPrice();
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
	public void printPrediction()
	{
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<BETA+1;j++)
			{
				System.out.print(pricePrediction[i][j] + " ");
			}
			System.out.println();
		}
		
	}
	public void setNewPrediction()
	{
		//pricePrediction = priceObservation;
		//priceObservation = new int[NUM_GOODS][BETA+1];
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<BETA+1;j++)
			{
				//System.out.println(pricePrediction[i][j]);
				prevPrediction[i][j] = pricePrediction[i][j];
				// 1 corresponds infestismal amount mentioned in the paper.
				pricePrediction[i][j] = priceObservation[i][j] + 1;
				priceObservation[i][j] = 0; 
			}
		}
		this.observationCount = 0;
	}
	public double getMaxDist()
	{
		double maxDist = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			double sumPrev = 0;
			double sumCurrent = 0;
			for (int j=0;j<BETA+1;j++)
			{
				sumPrev += ((double)prevPrediction[i][j]/(double)NUM_SIMULATION);
				sumCurrent += ((double)pricePrediction[i][j]/(double)NUM_SIMULATION);
				//System.out.println(sumPrev + " : " + sumCurrent);
				if (Math.abs(sumCurrent - sumPrev) > Math.abs(maxDist))
				{
					maxDist = Math.abs(sumCurrent - sumPrev);
				}
			}
		}
		return maxDist;
	}
	public void addObservation(InformationState s)
	{
		int[] finalPrice = s.getCurrentBidPrice();
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			this.priceObservation[i][finalPrice[i]]++;
		}
		this.observationCount++;
	}
	
	public int[] bid(InformationState s)
	{
		int[] newBid = new int[NUM_GOODS];
		int[] currentBid = s.getCurrentBidPrice();
		int[] currentWinning = s.getCurrentBidWinning();
		double[] currentPrediction = new double[NUM_GOODS];
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			// in case of winning
			if (currentWinning[i] == index)
			{
				int denomOutbid = 0;
				int denom = 0;
				for (int j=currentBid[i];j<BETA+1;j++)
				{
					denomOutbid += this.pricePrediction[i][j];
					if (j >= currentBid[i]+2) denom += this.pricePrediction[i][j];
				}
				double probOutbid = (1.0 - (double)this.pricePrediction[i][currentBid[i]] / (double)denomOutbid);
				
				double sum = 0;
				for (int j=currentBid[i]+2;j<BETA+1;j++)
				{
					sum += (j * (double)this.pricePrediction[i][j]/(double)denom);
				}
				//currentPrediction[i] = (int)Math.round(probOutbid * sum);
				currentPrediction[i] = (denomOutbid == 0) ? 0 : probOutbid * sum;
			}
			// in case of not winning
			else
			{
				int denom = 0;
				for (int j=currentBid[i]+1;j<BETA+1;j++)
				{
					denom += this.pricePrediction[i][j];
				}
				double expectedPrice = 0;
				for (int j=currentBid[i]+1;j<BETA+1;j++)
				{
					//expectedPrice += (int)Math.round(((double)j * (double)this.pricePrediction[i][j]/(double)denom));
					expectedPrice += ((double)j * (double)this.pricePrediction[i][j]/(double)denom);
				}
				currentPrediction[i] = (denom == 0) ? 0 : expectedPrice;
			}
		}
		
		// Initially play SB
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (currentPrediction[i] == 0 || this.isSingleUnitDemand)
				currentPrediction[i] = (currentWinning[i] == index) ?  (double)currentBid[i] : (double)currentBid[i] + 1;
		}
		/*
		System.out.print("Agent " + index + "'s prediction : ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentPrediction[i] + " ");
		}
		System.out.println();
		*/
		
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
