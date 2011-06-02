package org.srg.scpp_im.strategy;

import org.srg.scpp_im.game.InformationState;
import java.util.BitSet;
import java.util.Map;

public class SEQ_MDPOptimalV2 extends SelfConfirmingDistributionPricePrediction
{
	private static final long serialVersionUID = 100L;
	
	double[][] vTable;
	
	public SEQ_MDPOptimalV2(int index)
	{
		super(index);
		//vTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS];
		//qTable = new double[(int)Math.pow(2, NUM_GOODS)][NUM_GOODS][VALUE_UPPER_BOUND+1];
	}
	public void setTypeDist(Map<BitSet, Integer> typeDist, int length, int[] deadlineValues)
	{
		this.typeDist = typeDist;
		this.checkSingleDemand();
		this.jobLength = length;
		vTable = new double[length+1][NUM_GOODS+1];
		
		calculateDP(deadlineValues);
		
		if (PRINT_DEBUG)
		{
			for (int i=0;i<=jobLength;i++)
			{
				for (int j=0;j<=NUM_GOODS;j++)
				{
					System.out.print(vTable[i][j] + " ");
				}
				System.out.println();
			}
		}
	}
	
	private void calculateDP(int[] deadlineValues)
	{
		// initialize vTable
		for (int i=1;i<=NUM_GOODS;i++)
		{
			vTable[0][i] = deadlineValues[i-1];
		}
		for (int j=1;j<=jobLength;j++)
		{
			vTable[j][NUM_GOODS-1] = 0;
		}
		
		// fill vTable
		double b;
		for (int j=NUM_GOODS-1;j>=0;j--)
		{
			for (int i=1;i<=jobLength;i++)
			{
				b = vTable[i-1][j+1] - vTable[i][j+1];
				if (b<0) b = 0;
				double pr = this.cumulPrediction[i-1][(int)Math.floor(b)] / 
				this.cumulPrediction[i-1][VALUE_UPPER_BOUND];
				double c = getCost(b, j);
				
				vTable[i][j] = pr * (vTable[i-1][j+1] - c) + (1 - pr) * vTable[i][j+1];
			}
		}
	}
	
	private double getCost(double b, int j)
	{
		double c = 0.0;
		
		for (int i=0;i<=b;i++)
		{
			c += (this.pricePrediction[j][i] / this.cumulPrediction[j][VALUE_UPPER_BOUND] * i);
		}
		return c;
	}
	
	public double[] bid(InformationState state)
	{
		int numGoodWon = 0;
		double[] bids = new double[NUM_GOODS];
		BitSet holding = new BitSet();
		int[] winner = state.getCurrentBidWinning();
		double[] price = state.getCurrentBidPrice();
		int round = state.getRound();
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (winner[i] == this.getIndex()) numGoodWon++;
		}
		//System.out.println("Boundary check:" + jobLength + ", " + numGoodWon + ", " + round);	
		bids[round] = (numGoodWon >= jobLength) ? 0 : vTable[jobLength - numGoodWon - 1][round + 1] - 
				vTable[jobLength - numGoodWon][round+1]; 
		return bids;
	}
}
