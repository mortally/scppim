package org.srg.scpp_im.strategy;

import com.csvreader.*;
import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.BitSet;

public class SelfConfirmingPricePrediction extends GameSetting implements Serializable, Strategy {
	
	protected static final long serialVersionUID = 100L;

	protected int index;
	//private int[] typeDist;
	protected Map<BitSet, Integer> typeDist;
	protected boolean isSingleUnitDemand;
	//private double[] pricePrediction;
	protected double[] pricePrediction;
	protected double[] prevPrediction;
	protected BitSet[] bitVector;
	
	protected int[] priceObservation = new int[NUM_GOODS];
	protected int observationCount = 0;
	protected int cumulatedUtility = 0;
	protected final int prediction_type = POINT;
	
	public SelfConfirmingPricePrediction(int index)
	{
		this.index = index;
		this.isSingleUnitDemand = true;
		this.pricePrediction = new double[NUM_GOODS];
		this.prevPrediction = new double[NUM_GOODS];
		//temp
		//this.pricePrediction[1] = 15;
		//this.pricePrediction[0] = 14;
		
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
	
	public SelfConfirmingPricePrediction(int index, Map<BitSet, Integer> typeDist)
	{
		this.index = index;
		this.typeDist = typeDist;
		this.isSingleUnitDemand = true;
		this.checkSingleDemand();
		this.pricePrediction = new double[NUM_GOODS];
		this.prevPrediction = new double[NUM_GOODS];
	}
	
	public SelfConfirmingPricePrediction(int index, Map<BitSet, Integer> typeDist, double[] pp)
	{
		this.index = index;
		this.typeDist = typeDist;
		this.isSingleUnitDemand = true;
		this.checkSingleDemand();
		this.pricePrediction = pp;
		this.prevPrediction = new double[NUM_GOODS];
	}
	
	public int getIndex()
	{
		return index;
	}
	public String getName()
	{
		String stratName = this.getClass().getName();
		int firstChar = stratName.lastIndexOf('.') + 1;
		if (firstChar > 0) stratName = stratName.substring(firstChar);
		return stratName;
	}
	
	public String getPPName()
	{
		return this.getName();
	}
	
	public int getPredictionType()
	{
		return this.prediction_type;
	}
	
	public void setTypeDist(Map<BitSet, Integer> typeDist)
	{
		this.typeDist = typeDist;
		this.checkSingleDemand();
	}
	
	public Map<BitSet, Integer> getTypeDist()
	{
		return typeDist;
	}
	public void printPrediction()
	{
		System.out.print("Agent " + index + "'s PP: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(this.pricePrediction[i] + " ");
		}
		System.out.println();
	}
	
	public <T>void setPricePrediction(T pp)
	{
		this.prevPrediction = this.pricePrediction;
		this.pricePrediction = (double[])pp;
	}
	
	public double[] getPricePrediction()
	{
		return this.pricePrediction;
	}
	
	public void readPricePrediction(String file)
	{
		try
		{
			CsvReader cr = new CsvReader(file);
			cr.readRecord();
			if (cr.getColumnCount() != NUM_GOODS) throw new Exception("PP data has incorrect # of goods.");
			for (int i=0;i<NUM_GOODS;i++)
			{
				this.pricePrediction[i] = Double.parseDouble(cr.get(i));
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
			
	}
	
	public double getAverageUtility()
	{
		return (double)this.cumulatedUtility / (double)this.observationCount;
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
		int utility = value - cost;
		//cumulatedUtility += utility;
		return utility;
	}
	
	public void addObservation(InformationState s)
	{
		int[] finalPrice = s.getCurrentBidPrice();
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			priceObservation[i] += finalPrice[i];
		}
		this.observationCount++;
		cumulatedUtility += this.getCurrentSurplus(s);
	}
	
	public double getMaxDist()
	{
		double max_dist = 0;
		double diff;
		for (int i=0;i<NUM_GOODS;i++)
		{
			diff = this.pricePrediction[i] - this.prevPrediction[i];
			if (Math.abs(diff) > Math.abs(max_dist))
			{
				max_dist = diff;
			}
			//System.out.print(avgPrice[i] + " ");
		}
		return max_dist;
	}
	
	public void setNewPrediction()
	{
		//this.prevPrediction = this.pricePrediction;
		for (int i=0;i<NUM_GOODS;i++)
		{
			//this.pricePrediction[i] = (int)Math.round((double)this.priceObservation[i]/(double)this.observationCount);
			this.prevPrediction[i] = this.pricePrediction[i];
			this.pricePrediction[i] = (double)this.priceObservation[i]/(double)this.observationCount;
			this.priceObservation[i] = 0;
		}
		this.observationCount = 0;
	}
	
	public void setNewPredictionAverage(int currentIt)
	{
		for (int i=0;i<NUM_GOODS;i++)
		{
			//this.pricePrediction[i] = (int)Math.round((double)this.priceObservation[i]/(double)this.observationCount);
			//double prev = prevPrediction[i];
			this.prevPrediction[i] = this.pricePrediction[i];
			double diff = ((double)this.priceObservation[i]/(double)this.observationCount) - this.pricePrediction[i];
			diff = diff * (GameSetting.NUM_ITERATION - currentIt) * 2 / (GameSetting.NUM_ITERATION * 2); // decreases by 1/NUM_ITERATION each time
			this.pricePrediction[i] = this.pricePrediction[i] + diff;
			//this.pricePrediction[i] = (this.pricePrediction[i] + (double)this.priceObservation[i]/(double)this.observationCount) / 2.0;
			this.priceObservation[i] = 0;
		}
		this.observationCount = 0;
	}
	
	public void resetObservation()
	{
		for (int i=0;i<NUM_GOODS;i++)
		{
			this.priceObservation[i] = 0;
		}
		this.observationCount = 0;
		this.cumulatedUtility = 0;
	}
	
	public int[] bid(InformationState s)
	{
		int[] currentBid = s.getCurrentBidPrice();
		int[] currentWinning = s.getCurrentBidWinning();
		double[] currentPrediction = new double[NUM_GOODS];
		int[] newBid = new int[NUM_GOODS];
		/*
		for (int i=0;i<currentBid.length;i++)
		{
			if (currentWinning[i] == index)
			{
				newBid[i] = currentBid[i];
			}
			else
			{
				if (typeDist[i] - 1 > currentBid[i])
				{
					newBid[i] = currentBid[i] + 1;
				}
				else newBid[i] = 0;
			}
		}
		return newBid;
		*/
		//System.out.print("Agent " + index + "'s prediction : ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			//int value = (int)Math.abs(pricePrediction[i]);
			if (this.isSingleUnitDemand) currentPrediction[i] = (currentWinning[i] == index) ? (double)currentBid[i] : (double)currentBid[i] + 1;			
			else currentPrediction[i] = (currentWinning[i] == index) ? (pricePrediction[i] > (double)currentBid[i] ? pricePrediction[i] : (double)currentBid[i]) : (pricePrediction[i] > (double)currentBid[i] + 1 ? pricePrediction[i] : currentBid[i] + 1);
			//currentPrediction[i] = value;
			//System.out.print(currentPrediction[i] + " ");
		}
		//System.out.println();
		// Given type-distribution and current information state find the subset that gives highest surplus
		double max_surplus = Double.MIN_VALUE;
		BitSet maxSet = new BitSet();
		for (BitSet bs : bitVector)
		{
			/*BitSet bs = new BitSet();
			String bits = Integer.toBinaryString(i);
			bits = new StringBuffer(bits).reverse().toString();
			int[] bit = new int[bits.length()];
			for (int j=0;j<bits.length();j++)
			{
				char bitChar = bits.charAt(j);
				String bitStr = String.valueOf(bitChar);
				bit[j] = Integer.parseInt(bitStr);
				if (bit[j] == 1) bs.set(j, true);
				else bs.set(j, false);
			}*/
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
				else newBid[i] = currentBid[i]+1;
			}
			else newBid[i] = 0;
		}
		return newBid;
	}
	public boolean isSingleUnitDemand()
	{
		return this.isSingleUnitDemand;
	}
	protected void checkSingleDemand()
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
