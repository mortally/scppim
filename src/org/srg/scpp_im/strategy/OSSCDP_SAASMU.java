package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;
import	lpsolve.*; 

public class OSSCDP_SAASMU extends SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_SAASMU(int index)
	{
		super(index);
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
	
	public int[] bid(InformationState s)
	{
		int[] newBid = new int[NUM_GOODS];
		int[] singleGoodValue = new int[NUM_GOODS];
		int[] priceToBid = new int[NUM_GOODS];
		
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
		else
		{
			double[] sampleBid = doSAA();
			for (int i=0;i<NUM_GOODS;i++)
			{
				newBid[i] = (int)Math.round(sampleBid[i]);
			}
		}
		
		return newBid;
	}
	
	private double[] doSAA()
	{
		double[][] scenarios = new double[NUM_SCENARIO][NUM_GOODS];
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
					while (cumulPrediction[pos] == cumulPrediction[pos-1])
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
		
		int numSets = (int)Math.pow(2, NUM_GOODS);
		int numVar = numSets * NUM_SCENARIO;
		double[] coeffs = new double[numVar];
		
		for (int e=0;e<NUM_SCENARIO;e++)
		{
			for (int i=0;i<numSets;i++)
			{
				BitSet bs = bitVector[i];
				double value = (double)this.typeDist.get(bs).intValue();
				double price = 0;
				
				for (int k=0;k>NUM_GOODS;k++)
				{
					if (bs.get(k))
					{
						price += scenarios[e][k];
					}
				}
				coeffs[e*numSets+i] = value - price;
			}
		}
		
		try
		{
			LpSolve solver = LpSolve.makeLp(0, numVar);
			solver.setObjFn(coeffs);
			solver.setMaxim();
			
			// all variables are binary, bid or not.
			for (int i=0;i<numVar;i++)
			{
				solver.setBinary(i, true);
			}
			
			for (int e=0;e<NUM_SCENARIO;e++)
			{
				double[] constraint = new double[numVar];
				for (int i=e*numSets;i<(e+1)*numSets;i++)
				{
					constraint[i] = 1;
				}
				solver.addConstraint(constraint, LpSolve.LE, 1);
			}
			solver.solve();
			solver.deleteLp();
		}
		catch (LpSolveException e)
		{
			e.printStackTrace();
		}
		double[] temp = new double[NUM_GOODS];
		return temp;
	}
}
