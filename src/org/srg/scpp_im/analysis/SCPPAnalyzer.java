package org.srg.scpp_im.analysis;

import org.srg.scpp_im.game.InformationState;
import org.srg.scpp_im.game.GameSetting;
//import jsc.independentsamples.TwoSampleTtest;

public class SCPPAnalyzer extends GameSetting {
	
	private InformationState[] states;
	private double[][] utilities;
	private double[] optEffs;
	private double[] effs;
	private int numState;
	
	public SCPPAnalyzer()
	{
		optEffs = new double[NUM_ITERATION];
		effs = new double[NUM_ITERATION];
		states = new InformationState[NUM_SIMULATION];
		utilities = new double[NUM_SIMULATION][NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL];
		numState = 0;
	}
	
	public void addState(InformationState s)
	{
		states[numState] = s;
		numState++;
	}
	
	public void addUtility(int index, double utility)
	{
		utilities[numState][index-1] = utility;
	}
	
	public void addEff(int index, double eff)
	{
		effs[index] = eff;
	}
	
	public void optEffs(int index, double eff)
	{
		optEffs[index] = eff;
	}
	
	public void printUtilityStatistics()
	{
		int[] posFreq = new int[NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL];
		int[] negFreq = new int[NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL];
		double[] posUtil = new double[NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL];
		double[] negUtil = new double[NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL];
		
		for (int i=0;i<NUM_SIMULATION;i++)
		{
			for (int j=0;j<NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL;j++)
			{
				if (utilities[i][j] > 0)
				{
					posFreq[j]++;
					posUtil[j] += utilities[i][j];
				}
				else if (utilities[i][j] < 0)
				{
					negFreq[j]++;
					negUtil[j] += utilities[i][j];
				}
			}
		}
		
		System.out.print("Positive Utility Frequency: ");
		for (int j=0;j<NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL;j++)
		{
			System.out.print(posFreq[j] + " ");
		}
		System.out.println();
		
		System.out.print("Positive Utility Total: ");
		for (int j=0;j<NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL;j++)
		{
			System.out.print(posUtil[j] + " ");
		}
		System.out.println();
		
		System.out.print("Negative Utility Frequency: ");
		for (int j=0;j<NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL;j++)
		{
			System.out.print(negFreq[j] + " ");
		}
		System.out.println();
		
		System.out.print("Negative Utility Total: ");
		for (int j=0;j<NUM_AGENT*HIERARCHICAL_REDUCTION_LEVEL;j++)
		{
			System.out.print(negUtil[j] + " ");
		}
		System.out.println();
	}
}
