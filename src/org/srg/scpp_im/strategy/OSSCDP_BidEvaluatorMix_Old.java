package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;

public class OSSCDP_BidEvaluatorMix_Old extends
		SelfConfirmingDistributionPricePrediction {
	
	private static final long serialVersionUID = 100L;
	private static final int NUM_SAMPLE = 8;
	private static final int NUM_SCENARIO = 512;
	private static final int NUM_CANDIDATE_BID = 64;
	protected int prediction_type = DISTRIBUTION_MIX;
	protected double[][][] pricePredictionMix;
	protected double[][][] cumulPredictionMix;
	
	public OSSCDP_BidEvaluatorMix_Old(int index)
	{
		super(index);
		pricePredictionMix = new double[NUM_DIST_MIX][NUM_GOODS][VALUE_UPPER_BOUND+1];
		cumulPredictionMix = new double[NUM_DIST_MIX][NUM_GOODS][VALUE_UPPER_BOUND+1];
	}
	
	// Override
	public int getPredictionType()
	{
		return prediction_type;
	}
	
	// Override
	public String getPPName()
	{
		return "OSSCDP_DistributionMix";
	}
	
	// Override
	public <T>void setPricePrediction(T pp)
	{
		double[][][] newPP = (double[][][])pp;
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<VALUE_UPPER_BOUND;j++)
			{
				for (int k=0;k<NUM_DIST_MIX;k++)
				{
					this.pricePredictionMix[k][i][j] = newPP[k][i][j];
				}
			}
		}
		for (int i=0;i<NUM_GOODS;i++)
		{
			for (int j=0;j<VALUE_UPPER_BOUND;j++)
			{
				for (int k=0;k<NUM_DIST_MIX;k++)
				{
					if (this.pricePredictionMix[k][i][j] != 0)
					{
						this.isPricePredicting = true;
						buildCumulativeDistMix();
						return;
					}
				}
			}
		}
	}
	
	private void buildCumulativeDistMix()
	{
		for (int k=0;k<NUM_DIST_MIX;k++)
		{
			for (int i=0;i<NUM_GOODS;i++)
			{
				for (int j=0;j<VALUE_UPPER_BOUND+1;j++)
				{
					cumulPredictionMix[k][i][j] = 0;
					cumulPredictionMix[k][i][j] += pricePredictionMix[k][i][j];
					if (j>0)
					{
						cumulPredictionMix[k][i][j] += cumulPredictionMix[k][i][j-1];
					}
				}
			}
		}
	}
	
	public int[] bid(InformationState s)
	{
		int[] newBid = new int[NUM_GOODS];
		int[] singleGoodValue = new int[NUM_GOODS];
		int[] priceToBid = new int[NUM_GOODS];
		//int noPredCount = 0;
		
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
		double max_surplus = 0;
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
			double[] sampleBid = sampleMV();
			for (int i=0;i<NUM_GOODS;i++)
			{
				newBid[i] = (int)Math.round(sampleBid[i]);
			}
		}
		return newBid;
	}
	
	private double[] sampleMV()
	{
		double[][] scenarios = new double[NUM_SCENARIO][NUM_GOODS];
		//double[][] candidates = new double[NUM_CANDIDATE_BID][NUM_GOODS];
		//double[] sumPrice = new double[NUM_GOODS];
		//double[] average_price = new double[NUM_GOODS];
		//double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		int dist_type;
		
		// Sample E scenarios
		for (int e=0;e<NUM_SCENARIO;e++)
		{
			dist_type = ran.nextInt(NUM_DIST_MIX);
			double dist_num;
			
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPredictionMix[dist_type][i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPredictionMix[dist_type][i], dist_num);
				if (pos >= 0) 
				{
					while (cumulPredictionMix[dist_type][i][pos] == cumulPredictionMix[dist_type][i][pos-1])
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
		
		// consider K candidate bids
		double[] bestBid = new double[NUM_GOODS];
		double bestUtil = 0;
		for (int k=0;k<NUM_CANDIDATE_BID;k++)
		{
			double[] candidateBid;
			int bidType = ran.nextInt(3);
			switch(bidType)
			{
				case 0:
					candidateBid = straightMU();
					break;
				case 1:
					candidateBid = targetMU();
					break;
				case 2:
					candidateBid = targetMUStar();
					break;
				default:
					candidateBid = straightMU();
			}

			if (PRINT_DEBUG)
			{
				System.out.println("Agent " + this.getIndex() + "'s candidate bids");
				for (int i=0;i<NUM_GOODS;i++)
				{
					System.out.print(candidateBid[i] + " ");
				}
				System.out.println();
			}
			//boolean[] productWon = new boolean[NUM_GOODS];
			double totalUtil = 0.0;
			double utility = 0.0;
			double cost;
			for (int e=0;e<NUM_SCENARIO;e++)
			{
				cost = 0.0;
				BitSet productWon = new BitSet();
				for (int i=0;i<NUM_GOODS;i++)
				{
					if (candidateBid[i] > scenarios[e][i]) 
					{
						productWon.set(i);
						cost += scenarios[e][i];
					}
				}
				double value = typeDist.get(productWon) != null ? typeDist.get(productWon).intValue() : 0;
				utility = value - cost;
				totalUtil += utility/(double)NUM_SCENARIO;
			}
			if (totalUtil > bestUtil)
			{
				bestUtil = totalUtil;
				for (int i=0;i<NUM_GOODS;i++)
				{
					bestBid[i] = candidateBid[i];
				}
			}
		}
		if (PRINT_DEBUG)
		{
			System.out.println("Agent " + this.getIndex() + "'s best bid with utility = " + bestUtil + ":");
			for (int i=0;i<NUM_GOODS;i++)
			{
				System.out.print(bestBid[i] + " ");
			}
			System.out.println();
		}
		return bestBid;
	}
	
	private double[] straightMU()
	{
		double[] sumPrice = new double[NUM_GOODS];
		double[] average_price = new double[NUM_GOODS];
		double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		int dist_type;
		// Sample K scenarios
		for (int k=0;k<NUM_SAMPLE;k++)
		{
			double dist_num;
			dist_type = ran.nextInt(NUM_DIST_MIX);
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPredictionMix[dist_type][i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPredictionMix[dist_type][i], dist_num);
				if (pos >= 0) 
				{
					// need to handle when there are multiple identical elements.
					// backtrack for identical elements
					while (cumulPredictionMix[dist_type][i][pos] == cumulPredictionMix[dist_type][i][pos-1])
					{
						pos--;
					}
					sumPrice[i] += pos;
				}
				else
				{
					sumPrice[i] += (pos * -1) - 1;
				}
			}
		}
		// Get the expectation over price distribution, i.e. sampled K scenarios
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
		}
		
		// StraightMV on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = 0;
			double max_unavail_surplus = 0;
			for (BitSet bs : bitVector)
			{
				double free_surplus = 0;
				double unavail_surplus = 0;
				int value = typeDist.get(bs).intValue();
				double freeCost = 0.0;
				double unavailCost = 0.0;
				
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) 
					{
						if (i==j) unavailCost += Double.POSITIVE_INFINITY;
						else
						{
							freeCost += average_price[j];
							unavailCost += average_price[j];
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
			} // end for
			
			double margVal = max_free_surplus - max_unavail_surplus;
			mv[i] = margVal;
		}
		return mv;
	}
	
	private double[] targetMU()
	{
		double[] sumPrice = new double[NUM_GOODS];
		double[] average_price = new double[NUM_GOODS];
		double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		int dist_type;
		// Sample K scenarios
		for (int k=0;k<NUM_SAMPLE;k++)
		{
			double dist_num;
			dist_type = ran.nextInt(NUM_DIST_MIX);
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPredictionMix[dist_type][i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPredictionMix[dist_type][i], dist_num);
				if (pos >= 0) 
				{
					// need to handle when there are multiple identical elements.
					// backtrack for identical elements
					while (cumulPredictionMix[dist_type][i][pos] == cumulPredictionMix[dist_type][i][pos-1])
					{
						pos--;
					}
					sumPrice[i] += pos;
				}
				else
				{
					sumPrice[i] += (pos * -1) - 1;
				}
			}
		}
		
		// Get the expectation over price distribution, i.e. sampled K scenarios
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
		}
		
		// TargetMV on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = 0;
			double max_unavail_surplus = 0;
			for (BitSet bs : bitVector)
			{
				double free_surplus = 0;
				double unavail_surplus = 0;
				int value = typeDist.get(bs).intValue();
				double freeCost = 0.0;
				double unavailCost = 0.0;
				
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) 
					{
						if (i==j) unavailCost += Double.POSITIVE_INFINITY;
						else
						{
							freeCost += average_price[j];
							unavailCost += average_price[j];
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
			} // end for
			
			double margVal = max_free_surplus - max_unavail_surplus;
			mv[i] = margVal;
		}
		
		double max_surplus = 0;
		BitSet maxSet = new BitSet();
		for (BitSet bs : bitVector)
		{
			double value = (double)typeDist.get(bs).intValue();
			double cost = 0.0;
			for (int j=0;j<bs.length();j++)
			{
				if (bs.get(j)) cost += average_price[j];
			}
			value -= cost;
			if (value > max_surplus)
			{
				maxSet = bs;
				max_surplus = value;
			}
		}
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (max_surplus > 0 && maxSet.get(i)) 
			{
			}
			else mv[i] = 0; // reset bid for item to 0 if it is not a part of optimal solution.
		}
		
		return mv;
	}
	
	private double[] targetMUStar()
	{
		double[] sumPrice = new double[NUM_GOODS];
		double[] average_price = new double[NUM_GOODS];
		double[] mv = new double[NUM_GOODS];
		Random ran = new Random();
		int dist_type;
		// Sample K scenarios
		for (int k=0;k<NUM_SAMPLE;k++)
		{
			double dist_num;
			dist_type = ran.nextInt(NUM_DIST_MIX);
			for (int i=0;i<NUM_GOODS;i++)
			{
				dist_num = cumulPredictionMix[dist_type][i][VALUE_UPPER_BOUND] * ran.nextDouble();
				int pos = Arrays.binarySearch(cumulPredictionMix[dist_type][i], dist_num);
				if (pos >= 0) 
				{
					// need to handle when there are multiple identical elements.
					// backtrack for identical elements
					while (cumulPredictionMix[dist_type][i][pos] == cumulPredictionMix[dist_type][i][pos-1])
					{
						pos--;
					}
					sumPrice[i] += pos;
				}
				else
				{
					sumPrice[i] += (pos * -1) - 1;
				}
			}
		}
		
		// Get the expectation over price distribution, i.e. sampled K scenarios
		for (int i=0;i<NUM_GOODS;i++)
		{
			average_price[i] = sumPrice[i] / (double)NUM_SAMPLE;
		}
		
		double max_surplus = 0;
		BitSet maxSet = new BitSet();
		for (BitSet bs : bitVector)
		{
			double value = (double)typeDist.get(bs).intValue();
			double cost = 0.0;
			for (int j=0;j<bs.length();j++)
			{
				if (bs.get(j)) cost += average_price[j];
			}
			value -= cost;
			if (value > max_surplus)
			{
				maxSet = bs;
				max_surplus = value;
			}
		}
		// TargetMV* on the average scenario
		for (int i=0;i<NUM_GOODS;i++)
		{
			double max_free_surplus = 0;
			double max_unavail_surplus = 0;
			for (BitSet bs : bitVector)
			{
				double free_surplus = 0;
				double unavail_surplus = 0;
				int value = typeDist.get(bs).intValue();
				double freeCost = 0.0;
				double unavailCost = 0.0;
				
				for (int j=0;j<bs.length();j++)
				{
					if (bs.get(j)) 
					{
						if (i==j) unavailCost += Double.POSITIVE_INFINITY;
						else
						{
							if (maxSet.get(j))
							{
								freeCost += average_price[j];
								unavailCost += average_price[j];
							}
							else
							{
								freeCost += Double.POSITIVE_INFINITY;
								unavailCost += Double.POSITIVE_INFINITY;
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
			} // end for
			
			double margVal = max_free_surplus - max_unavail_surplus;
			mv[i] = margVal > 0 ? (int)Math.round(margVal) : 0;
		}
		
		for (int i=0;i<NUM_GOODS;i++)
		{
			if (max_surplus > 0 && maxSet.get(i)) 
			{
			}
			else mv[i] = 0; // reset bid for item to 0 if it is not a part of optimal solution.
		}
		
		return mv;
	}
}
