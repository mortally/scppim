package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;
import	lpsolve.*; 

public class OSSCDP_SAA extends SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_SAA(int index)
	{
		super(index);
	}

	// Override
	/*
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
	*/
	
	public double[] bid(InformationState s)
	{
		//double[] newBid = new int[NUM_GOODS];
		//int[] singleGoodValue = new int[NUM_GOODS];
		//int[] priceToBid = new int[NUM_GOODS];
		
		/*
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
		double max_surplus = Double.MIN_VALUE;
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
		int[] sampleBid = doSAA();
		
		double[] bid = new double[NUM_GOODS];
		for (int i=0;i<NUM_GOODS;i++)
		{
			bid[i] = (double)sampleBid[i];
		}
		
		return bid;
	}
	
	private int[] doSAA()
	{
		double[][] scenarios = new double[NUM_SCENARIO][NUM_GOODS];
		int[] maxBid = new int[NUM_GOODS];
		//BitSet maxBitSet = null;
		Random ran = new Random();
		
		// Sample E scenarios
		for (int e=0;e<NUM_SCENARIO;e++)
		{
			double dist_num;
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPrediction[i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPrediction[i], dist_num);
				if (pos >= 0) 
				{
					while (cumulPrediction[i][pos] == cumulPrediction[i][pos-1])
					{
						pos--;
					}
					scenarios[e][i] = pos;
				}
				else
				{
					scenarios[e][i] = ((pos * -1) - 1);
				}
			}
		}
		
		int bidLevel = 6;
		int[] bidVal = {0, 10, 20, 30, 40, 50};
		
		int numSets = (int)Math.pow(2, NUM_GOODS);
		int numVar = (int)Math.pow(bidLevel, NUM_GOODS);
		//int numVar = numSets; // * NUM_SCENARIO;
		double[] coeffs = new double[numVar];
		
		int[] thresh = new int[NUM_GOODS];
		for (int i=0;i<NUM_GOODS;i++)
		{
			thresh[i] = (int)Math.pow(bidLevel, i);
		}
		
		for (int e=0;e<NUM_SCENARIO;e++)
		{
			for (int i=0;i<numVar;i++)
			{
				//System.out.println("==" + i + "==");
				int bidCoeff = i;
				int bids[] = new int[NUM_GOODS];
				for (int j=NUM_GOODS-1;j>=0;j--)
				{
					if (thresh[j] <= bidCoeff)
					{
						bids[j] = bidCoeff / thresh[j];
						bidCoeff = bidCoeff - bids[j] * thresh[j];
					}
				}
				
				BitSet bs = new BitSet();
				double price = 0.0;
				for (int j=0;j<NUM_GOODS;j++)
				{
					//System.out.println(j + " " + bids[j]);
					bids[j] = bidVal[bids[j]];
					if (bids[j] > scenarios[e][j]) 
					{
						bs.set(j);
						price += scenarios[e][j];
					}
				}
				
				double value = (double)this.typeDist.get(bs).intValue();
				
				coeffs[i] += (value - price);
			}
		}
		
		try
		{
			LpSolve solver = LpSolve.makeLp(0, numVar);
			solver.setObjFn(coeffs);
			solver.setMaxim();
			solver.setVerbose(0);
			// all variables are binary, bid or not.
			for (int i=1;i<=numVar;i++)
			{
				solver.setBinary(i, true);
			}
			
			double[] constraint = new double[numVar];
			for (int i=0;i<numVar;i++)
			{
				constraint[i] = 1;
			}
			solver.addConstraint(constraint, LpSolve.LE, 1);
			solver.solve();
			double[] var = solver.getPtrVariables();
			System.out.println("Value of objective function: " + solver.getObjective());
			/*
			System.out.println("Value of objective function: " + solver.getObjective());
			double sum = 0;
		      for (int i = 0; i < var.length; i++) {
		       // System.out.println("Value of var[" + i + "] = " + var[i]);
		        sum += var[i];
		      }
		    System.out.println("Sum = " + sum);
		    */
			int bundleToBid = -1;
			for (int i=0;i<numVar;i++)
			{
				//System.out.print(var[i] + " ");
				if (var[i] == 1) bundleToBid = i;
			}
			//System.out.println();
			//System.out.println("Bundle to bid = " + bundleToBid);
			//int[] bids = new int[NUM_GOODS];
			int bidCoeff = bundleToBid;
			for (int j=NUM_GOODS-1;j>=0;j--)
			{
				if (thresh[j] <= bidCoeff)
				{
					maxBid[j] = bidCoeff / thresh[j];
					bidCoeff = bidCoeff - maxBid[j] * thresh[j];
				}
			}
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				maxBid[i] = bidVal[maxBid[i]];
			}
			
			double surplus = solver.getObjective();
			for (int k=0;k<NUM_GOODS;k++)
			{
				if (surplus < 0) // || !maxBitSet.get(k))
				{
					maxBid[k] = 0;
				}
			}
			
			solver.deleteLp();
		}
		catch (LpSolveException e)
		{
			e.printStackTrace();
		}
		//double[] temp = new double[NUM_GOODS];
		return maxBid;
	}
}
