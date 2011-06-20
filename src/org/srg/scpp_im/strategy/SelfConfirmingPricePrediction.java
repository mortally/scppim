package org.srg.scpp_im.strategy;

import com.csvreader.*;
import org.srg.scpp_im.game.Strategy;
import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
import java.io.Serializable;
import java.io.File;
import java.util.Map;
import java.util.BitSet;
import java.util.Random;

/**
 * The base class for point price prediction strategies
 * @author Dong Young Yoon
 */
public class SelfConfirmingPricePrediction extends GameSetting implements Serializable, Strategy {
	
	/** The Constant serialVersionUID. */
	protected static final long serialVersionUID = 100L;

	/** The index of an agent. */
	protected int index;
	
	/** The required job length. */
	protected int jobLength;
	/** The type/value distribution. */
	protected Map<BitSet, Integer> typeDist;
	
	/** The is single unit demand. */
	protected boolean isSingleUnitDemand;
	/** The price prediction vector. */
	protected double[] pricePrediction;
	
	/** The previous price prediction vector. */
	protected double[] prevPrediction;
	
	/** The bit vector representing set of goods. */
	protected BitSet[] bitVector;
	
	/** The observed price vector. */
	protected double[] priceObservation = new double[NUM_GOODS];
	
	/** The utility record. */
	protected double[] utilityRecord = new double[NUM_SIMULATION];
	
	/** The observation count. */
	protected int observationCount = 0;
	
	/** The cumulated utility. */
	protected double cumulatedUtility = 0;
	
	/** The cumulated value. */
	protected double cumulatedValue = 0;
	
	/** The prediction type. */
	protected final int prediction_type = POINT;
	
	/**
	 * Instantiates a new agent with a point price prediction strategy.
	 *
	 * @param index the index of an agent
	 */
	public SelfConfirmingPricePrediction(int index)
	{
		this.index = index;
		this.isSingleUnitDemand = true;
		this.pricePrediction = new double[NUM_GOODS];
		this.prevPrediction = new double[NUM_GOODS];
		
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
		if (RANDOM_INITIAL_PREDICTION)
		{
			Random r = new Random();
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				this.pricePrediction[i] = r.nextInt(VALUE_UPPER_BOUND+1);
			}
		}
	}
	
	/**
	 * Instantiates a new agent with a point price prediction strategy.
	 *
	 * @param index the index of an agent
	 * @param typeDist the type/value distribution of an agent
	 */
	public SelfConfirmingPricePrediction(int index, Map<BitSet, Integer> typeDist)
	{
		this.index = index;
		this.typeDist = typeDist;
		this.isSingleUnitDemand = true;
		this.checkSingleDemand();
		this.pricePrediction = new double[NUM_GOODS];
		this.prevPrediction = new double[NUM_GOODS];
	}
	
	/**
	 * Instantiates a new agent with a point price prediction strategy.
	 *
	 * @param index the index
	 * @param typeDist the type/value distribution of an agent
	 * @param pp the price prediction vector
	 */
	public SelfConfirmingPricePrediction(int index, Map<BitSet, Integer> typeDist, double[] pp)
	{
		this.index = index;
		this.typeDist = typeDist;
		this.isSingleUnitDemand = true;
		this.checkSingleDemand();
		this.pricePrediction = pp;
		this.prevPrediction = new double[NUM_GOODS];
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getIndex()
	 */
	public int getIndex()
	{
		return index;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getName()
	 */
	public String getName()
	{
		String stratName = this.getClass().getName();
		int firstChar = stratName.lastIndexOf('.') + 1;
		if (firstChar > 0) stratName = stratName.substring(firstChar);
		return stratName;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getPPName()
	 */
	public String getPPName()
	{
		return this.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getPredictionType()
	 */
	public int getPredictionType()
	{
		return this.prediction_type;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#setTypeDist(java.util.Map)
	 */
	public void setTypeDist(Map<BitSet, Integer> typeDist)
	{
		this.typeDist = typeDist;
		this.checkSingleDemand();
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getTypeDist()
	 */
	public Map<BitSet, Integer> getTypeDist()
	{
		return typeDist;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#setTypeDist(java.util.Map, int, int[])
	 */
	public void setTypeDist(Map<BitSet, Integer> typeDist, int length, int[] deadlineValues)
	{
		this.jobLength = length;
		this.setTypeDist(typeDist);
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#printPrediction()
	 */
	public void printPrediction()
	{
		System.out.print("Agent " + index + "'s PP: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(this.pricePrediction[i] + " ");
		}
		System.out.println();
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#setPricePrediction(java.lang.Object)
	 */
	public <T>void setPricePrediction(T pp)
	{
		this.prevPrediction = this.pricePrediction;
		this.pricePrediction = (double[])pp;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getPricePrediction()
	 */
	public double[] getPricePrediction()
	{
		return this.pricePrediction;
	}
	
	/**
	 * Read price prediction from a file.
	 *
	 * @param file the file
	 */
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
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getAverageUtility()
	 */
	public double getAverageUtility()
	{
		return (double)this.cumulatedUtility / (double)this.observationCount;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getAverageValue()
	 */
	public double getAverageValue()
	{
		return this.cumulatedValue / (double)this.observationCount;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getCurrentSurplus(org.srg.scpp_im.game.InformationState)
	 */
	public double getCurrentSurplus(InformationState s)
	{
		double[] currentBid = s.getCurrentBidPrice();
		int[] currentWinning = s.getCurrentBidWinning();
		
		BitSet bs = new BitSet();
		double cost = 0.0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (currentWinning[i] == index)
			{
				bs.set(i);
				cost += currentBid[i];
			}
		}
		double value;
		value = typeDist.get(bs) != null ? typeDist.get(bs).intValue() : 0;
		double utility = value - cost;
		this.utilityRecord[this.observationCount] = utility;
		cumulatedValue += value;
		//cumulatedUtility += utility;
		return utility;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getUtilityRecord()
	 */
	public double[] getUtilityRecord()
	{
		return this.utilityRecord;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#addObservation(org.srg.scpp_im.game.InformationState)
	 */
	public void addObservation(InformationState s)
	{
		double[] finalPrice;
		if (HIGHEST_BID_PREDICTION) finalPrice = s.getCurrentTopAvgPrice(); 
		else finalPrice = s.getCurrentBidPrice();
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			priceObservation[i] += finalPrice[i];
		}
		cumulatedUtility += this.getCurrentSurplus(s);
		this.observationCount++;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getMaxDist()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#getMaxDist(double[][])
	 */
	public double getMaxDist(double pp[][])
	{
		double max_dist = 0;
		double diff;
		double[] avgPrices = new double[NUM_GOODS];
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<NUM_SIMULATION;j++)
			{
				avgPrices[i] += pp[j][i];
			}
			avgPrices[i] = avgPrices[i] / (double)NUM_SIMULATION;
			diff = avgPrices[i] - this.pricePrediction[i];
			if (Math.abs(diff) > Math.abs(max_dist))
			{
				max_dist = diff;
			}
		}
		return max_dist;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#setNewPrediction()
	 */
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
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#setNewPredictionAverage(int)
	 */
	public void setNewPredictionAverage(int currentIt)
	{
		for (int i=0;i<NUM_GOODS;i++)
		{
			//this.pricePrediction[i] = (int)Math.round((double)this.priceObservation[i]/(double)this.observationCount);
			//double prev = prevPrediction[i];
			this.prevPrediction[i] = this.pricePrediction[i];
			double diff = ((double)this.priceObservation[i]/(double)this.observationCount) - this.pricePrediction[i];
			diff = diff * (GameSetting.NUM_ITERATION - currentIt) / (GameSetting.NUM_ITERATION); // decreases by 1/NUM_ITERATION each time
			this.pricePrediction[i] = this.pricePrediction[i] + diff;
			//this.pricePrediction[i] = (this.pricePrediction[i] + (double)this.priceObservation[i]/(double)this.observationCount) / 2.0;
			this.priceObservation[i] = 0;
		}
		this.observationCount = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#resetObservation()
	 */
	public void resetObservation()
	{
		for (int i=0;i<NUM_GOODS;i++)
		{
			this.priceObservation[i] = 0;
		}
		this.observationCount = 0;
		this.cumulatedValue = 0;
		this.cumulatedUtility = 0;
	}
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#bid(org.srg.scpp_im.game.InformationState)
	 */
	public double[] bid(InformationState s)
	{
		double[] newBid = new double[NUM_GOODS];
		
		double[] currentBid = s.getCurrentBidPrice();
		int[] currentWinning = s.getCurrentBidWinning();
		double[] currentPrediction = new double[NUM_GOODS];
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
	
	/* (non-Javadoc)
	 * @see org.srg.scpp_im.game.Strategy#isSingleUnitDemand()
	 */
	public boolean isSingleUnitDemand()
	{
		return this.isSingleUnitDemand;
	}
	
	/**
	 * Check if an agent has a single-unit demand.
	 */
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
