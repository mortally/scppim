package org.srg.scpp_im.analysis;

import java.io.*;
import java.util.ArrayList;
import jsc.independentsamples.TwoSampleTtest;
import jsc.tests.H1;
import jsc.onesample.NormalMeanCI;
import jsc.datastructures.PairedData;
import jsc.onesample.WilcoxonTest;
import com.csvreader.*;
import org.srg.scpp_im.game.*;

public class StatAnalysis extends GameSetting
{
	public StatAnalysis()
	{
		
	}
	
	public void TwoSampleTtest(String file1, String file2)
	{
		ArrayList<Double> a = new ArrayList<Double>();
		ArrayList<Double> b = new ArrayList<Double>();
		
		try
		{
			FileReader fr1 = new FileReader(file1);
			BufferedReader br1 = new BufferedReader(fr1);
			String line;
			
			while ((line = br1.readLine()) != null)
			{
				a.add(Double.parseDouble(line));
			}
			
			FileReader fr2 = new FileReader(file2);
			BufferedReader br2 = new BufferedReader(fr2);
			
			while ((line = br2.readLine()) != null)
			{
				b.add(Double.parseDouble(line));
			}
			
			br1.close();
			br2.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		/*
		if (a.size() != b.size()) 
		{
			System.out.println("ERROR: Sample size mismatch!");
			System.exit(-1);
		}
		*/
		double[] xA = new double[a.size()];
		double[] xB = new double[b.size()];
		
		for (int i=0;i<a.size();i++)
		{
			xA[i] = a.get(i);
		}
		for (int i=0;i<b.size();i++)
		{
			xB[i] = b.get(i);
		}
		
		TwoSampleTtest t = new TwoSampleTtest(xA, xB, H1.LESS_THAN, false, 0.99);
		
		System.out.println("Signifiance probability = " + t.getSP());
		System.out.println("Test Statistic = " + t.getStatistic());
		System.out.println("Test Statistic = " + t.getTestStatistic());
		System.out.println("Lower CI Limit = " + t.getLowerLimit());
		System.out.println("Upper CI Limit = " + t.getUpperLimit());
		System.out.println("Mean A = " + t.getMeanA());
		System.out.println("Mean B = " + t.getMeanB());
		System.out.println("Var A = " + Math.pow(t.getSdA(),2));
		System.out.println("Var B = " + Math.pow(t.getSdB(),2));
		NormalMeanCI nm = new NormalMeanCI(xA, 0.99);
		System.out.println("Lower CI Limit = " + nm.getLowerLimit());
		System.out.println("Upper CI Limit = " + nm.getUpperLimit());
		
		PairedData pd = new PairedData(xA, xB);
		
		WilcoxonTest wt = new WilcoxonTest(pd, H1.LESS_THAN);
		System.out.println("== Wilcoxon matched pairs test ==");
		System.out.println("Approximiate significance probability = " + wt.approxSP());
		
	}

	public void priceDistAnalyze(String file, int numPred)
	{
		//System.out.println("nunPred = " + numPred);
		try
		{
			CsvReader cr = new CsvReader(file);
			double[][][] pp = new double[numPred][NUM_GOODS][VALUE_UPPER_BOUND+1];
			for (int p=0;p<numPred;p++)
			{
				for (int m=0;m<NUM_GOODS;m++)
				{
					cr.readRecord();
					//System.out.println("count = " + cr.getColumnCount());
					for (int v=0;v<VALUE_UPPER_BOUND+1;v++)
					{
						pp[p][m][v] = Double.parseDouble(cr.get(v));
					}
				}
			}
			cr.close();
			double avgMaxDist = 0.0;
			double maxDist = 0.0;
			int count = 0;
			// check every pair of price prediction vectors
			for (int i=0;i<numPred-1;i++)
			{
				for (int j=i+1;j<numPred;j++)
				{
					count++;
					double dist = this.getMaxDist(pp[i], pp[j]);
					System.out.println("Dist = " + dist + ", " + count);
					avgMaxDist += dist;
					if (maxDist < dist) maxDist = dist;
				}
			}
			System.out.println("Max KS Dist = " + maxDist);
			System.out.println("Avg KS Dist = " + avgMaxDist / (double)count);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private double getMaxDist(double[][] pp1, double[][] pp2)
	{
		double maxDist = 0.0;
		for (int i=0;i<NUM_GOODS;i++)
		{
			double total1 = 0.0;
			double total2 = 0.0;
			for (int j=0;j<VALUE_UPPER_BOUND+1;j++)
			{
				total1 += pp1[i][j];
				total2 += pp2[i][j];
			}
			double sum1 = 0.0;
			double sum2 = 0.0;
			for (int j=0;j<VALUE_UPPER_BOUND+1;j++)
			{
				sum1 += (pp1[i][j]/total1);
				sum2 += (pp2[i][j]/total2);
				if (Math.abs(sum1 - sum2) > maxDist)
				{
					maxDist = Math.abs(sum1 - sum2);
				}
			}
		}
		return maxDist;
	}
}
