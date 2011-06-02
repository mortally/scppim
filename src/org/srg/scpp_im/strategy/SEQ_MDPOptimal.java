package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.InformationState;
import java.util.BitSet;

public class SEQ_MDPOptimal extends SelfConfirmingDistributionPricePrediction
{
	private static final long serialVersionUID = 100L;
	
	double[][] vTable;
	double[][][] qTable;
	
	public SEQ_MDPOptimal(int index)
	{
		super(index);
		vTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS];
		qTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS][VALUE_UPPER_BOUND+1];
	}
	
	public double[] bid(InformationState state)
	{
		BitSet holding = new BitSet();
		int[] winner = state.getCurrentBidWinning();
		double[] price = state.getCurrentBidPrice();
		int round = state.getRound();
		// initialize tables for a new auction sequence
		if (round == 0)
		{
			vTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS];
			qTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS][VALUE_UPPER_BOUND+1];
		}
		
		for (int i=0;i<round;i++)
		{
			if (winner[i] == this.index)
			{
				holding.set(i);
			}
		}
		
		double[] bid = new double[NUM_GOODS];
		
		bid[round] = getPolicy(holding, round);
		
		return bid;
	}
	
	private double getPolicy(BitSet holding, int round)
	{
		double maxQ = Double.NEGATIVE_INFINITY;
		double maxBid = 0.0;
		
		int val = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (holding.get(i)) val += (int)Math.pow(2, i);
		}
		
		BitSet newHolding = (BitSet)holding.clone();
		newHolding.set(round);
		
		/*
		for (int b = 0;b <= VALUE_UPPER_BOUND; b++)
		{
			double q = getQ(holding, round, b);
			if (maxQ < q)
			{
				maxQ = q;
				maxBid = b;
			}
			System.out.println("b = " + b + ", qVal = " + q);
		}
		if (maxQ > 0) return maxBid;
		else return 0;*/
		//System.out.println("Agent " + this.getIndex() + "'s holdings = " + holding + ", " + newHolding + " at round " + round);
		double bStar = getV(newHolding, round+1) - getV(holding, round+1);
		//System.out.println(this.getIndex() + "'s b* =  " + bStar);
		if (bStar > 0) return bStar;
		else return 0;
	}
	
	private double getQ(BitSet holding, int round, int bid)
	{
		if (round == NUM_GOODS) return this.typeDist.get(holding).doubleValue();//getReward(holding, round, bid);
		
		int val = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (holding.get(i)) val += (int)Math.pow(2, i);
		}
		
		if (qTable[val][round][bid] != 0)
			return qTable[val][round][bid];
		
		BitSet newHolding = (BitSet)holding.clone();
		newHolding.set(round);
		
		double pr = this.cumulPrediction[round][(int)Math.floor(bid)] / 
		this.cumulPrediction[round][VALUE_UPPER_BOUND];
		
		qTable[val][round][bid] = getReward(holding, round, bid) + 
		pr * getV(newHolding, round+1) +
		(1 - pr) * getV(holding, round+1);
		
		return qTable[val][round][bid];
	}
	
	private double getReward(BitSet holding, int round, int bid)
	{
		if (round == NUM_GOODS) return this.typeDist.get(holding).doubleValue();
		
		double cumulReward = 0.0;
		double pr;
		for (int p=0;p <= VALUE_UPPER_BOUND; p++)
		{
			pr = this.pricePrediction[round][p] / this.cumulPrediction[round][VALUE_UPPER_BOUND];
			cumulReward += (bid >= p ? (-1 * p) * pr : 0.0);
		}
		return cumulReward;
	}
	
	private double getV(BitSet holding, int round)
	{
		if (round == NUM_GOODS) return this.typeDist.get(holding).doubleValue();
		
		int val = 0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (holding.get(i)) val += (int)Math.pow(2, i);
		}
		//System.out.println(val);
		if (vTable[val][round] != 0) return vTable[val][round];
		//if (vTable[(int)val][round][(int)payment * -1] != 0) return vTable[(int)val][round][(int)payment * -1]; 
		//else
		{
			double maxQ = Double.NEGATIVE_INFINITY;
			for (int b=0;b<=VALUE_UPPER_BOUND;b++)
			{
				double q = getQ(holding, round, b);
				//System.out.println(round + " " + holding + " " + b + " " + payment + " " + q);				
				if (maxQ < q) maxQ = q;
			}
			
			vTable[val][round] = maxQ; 
			return maxQ;
		}
	}
	/*
	private static final long serialVersionUID = 100L;
	//InformationState curState;
	double[][][] vTable;
	double[][][][] qTable;
	
	public SEQ_MDPOptimal(int index)
	{
		super(index);
		vTable = new double[(int)Math.pow(NUM_GOODS, 2)][NUM_GOODS][VALUE_UPPER_BOUND+1];
		qTable = new double[(int)Math.pow(NUM_GOODS, 2)][NUM_GOODS][VALUE_UPPER_BOUND+1][VALUE_UPPER_BOUND+1];
	}
	
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
	
	public double[] bid(InformationState state)
	{
		BitSet holding = new BitSet();
		int[] winner = state.getCurrentBidWinning();
		double[] price = state.getCurrentBidPrice();
		int round = state.getRound();
		if (round == 0)
		{
			vTable = new double[(int)Math.pow(NUM_GOODS, 2)][NUM_GOODS][VALUE_UPPER_BOUND+1];
			qTable = new double[(int)Math.pow(NUM_GOODS, 2)][NUM_GOODS][VALUE_UPPER_BOUND+1][VALUE_UPPER_BOUND+1];
		}
		double payment = 0.0;
		for (int i=0;i<round;i++)
		{
			if (winner[i] == this.index)
			{
				holding.set(i);
				payment += price[i];
			}
		}
		
		double[] bid = new double[NUM_GOODS];
		
		bid[round] = getPolicy(holding, round, (int)payment);
		
		return bid;
	}
	
	private double getPolicy(BitSet holding, int round, int payment)
	{
		double bid = -1;
		double maxArg = Double.NEGATIVE_INFINITY; 
		for (int b=0;b<VALUE_UPPER_BOUND - payment;b++)
		{
			double qVal = getQ(holding, round, b, 0);
			if (maxArg < qVal)
			{
				maxArg = qVal;
				bid = b;
			}
			/*
			else if (maxArg > qVal)
			{
				return b-1;
			}
			System.out.println("b = " + b + ", qVal = " + qVal);
		}
		if (bid > 0) return bid;
		else return 0;
	}
	
	private double getQ(BitSet holding, int round, double b, double payment)
	{
		double val = 0.0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (holding.get(i)) val += Math.pow(2, i);
		}
		//if (qTable[(int)val][round][(int)b][(int)payment * -1] != 0) return qTable[(int)val][round][(int)b][(int)payment * -1];
		BitSet newHolding = (BitSet)holding.clone();
		newHolding.set(round);
		double pr = this.cumulPrediction[round][(int)Math.floor(b)] / 
			this.cumulPrediction[round][VALUE_UPPER_BOUND];
		System.out.println(round + " " + b + " " +  pr);
		qTable[(int)val][round][(int)b][(int)payment * -1] =  (pr * getV(newHolding, round+1, payment-b)) 
			+ ((1-pr) * getV(holding, round+1, payment));
		return qTable[(int)val][round][(int)b][(int)payment * -1];
	}
	
	private double getV(BitSet holding, int round, double payment)
	{
		if (round == NUM_GOODS) return this.typeDist.get(holding).doubleValue() + payment;
		double val = 0.0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (holding.get(i)) val += Math.pow(2, i);
		}
		//if (vTable[(int)val][round][(int)payment * -1] != 0) return vTable[(int)val][round][(int)payment * -1]; 
		//else
		{
			double maxQ = Double.NEGATIVE_INFINITY;
			for (int b=0;b<VALUE_UPPER_BOUND+payment;b++)
			{
				double q = getQ(holding, round, b, payment);
				//System.out.println(round + " " + holding + " " + b + " " + payment + " " + q);				
				if (maxQ < q) maxQ = q;
			}
			
			vTable[(int)val][round][(int)payment * -1] = maxQ; 
			return maxQ;
		}
	}
	*/
}
