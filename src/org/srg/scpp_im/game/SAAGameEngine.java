package org.srg.scpp_im.game;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringWriter;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.Iterator;
import java.util.BitSet;
import java.util.Map;
import java.util.HashMap;

import org.srg.scpp_im.analysis.SCPPAnalyzer;
import org.srg.scpp_im.strategy.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import	lpsolve.*;

public class SAAGameEngine extends GameSetting implements Register {
	/*
	private static int NUM_AGENT = 3;
	private static int NUM_GOODS = 3;
	private static long NUM_SIMULATION = 1000000;
	private static int VALUE_UPPER_BOUND = 50;*/
	protected ArrayList<Strategy> strategies;
	
	protected int mode;
	protected int simulCount;
	protected int numAgentsReceived;
	protected double[] avgPrice;
	protected double[][] prices;
	protected int[] pp;
	protected int distCount;
	protected int[] sumValue;
	protected double[][] payoffRecord;
	protected double[] averagePayoff;
	protected BitSet[] bitVector;
	protected PrintStream payoff_out;
	protected String pp_path = "pp_data/";
	protected String pp_output_path = "pp_output/";
	protected SCPPAnalyzer analyzer = null;
	protected double sumOptValue;
	
	public SAAGameEngine()
	{
		//System.out.println("default cons");
		numAgentsReceived = 0;
		sumOptValue = 0;
		simulCount = 0;
		avgPrice = new double[NUM_GOODS];
		prices = new double[NUM_SIMULATION][NUM_GOODS];
		sumValue = new int[NUM_GOODS];
		payoffRecord = new double[NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL][NUM_ITERATION];
		averagePayoff = new double[NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL];
		distCount = 0;
		pp = new int[NUM_GOODS];
		strategies = new ArrayList<Strategy>();
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
	
	public SAAGameEngine(int mode)
	{
		/*
		numAgentsReceived = 0;
		avgPrice = new double[NUM_GOODS];
		sumValue = new int[NUM_GOODS];
		distCount = 0;
		pp = new int[NUM_GOODS];
		strategies = new ArrayList<Strategy>();
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
		}*/
		this();
		//System.out.println("arg cons");
		//strategies = new ArrayList<Strategy>();
		this.mode = mode;
		try
		{
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				OutputStream os = new FileOutputStream(new File(SIMUL_PATH + "/payoff_data"));
				payoff_out = new PrintStream(os);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void register(Strategy s)
	{
		if (this.mode == GameSetting.TRAINING_MODE && numAgentsReceived == NUM_AGENT) return;
		if (this.mode == GameSetting.PRODUCTION_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) return;
		// get Strategy object here
		
		// temporary setting of initial dist
		//this.initPricePrediction(s);
		try{
			strategies.add(s);
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
		System.out.println("Strategy added with index = " + s.getIndex());
		/*
		int[] typeDist = s.getTypeDist();
		for (int i=0;i<typeDist.length;i++)
    	{
        	System.out.print(typeDist[i] + " ");
    	}
        System.out.println();
        */
		numAgentsReceived++;
		if ((this.mode == GameSetting.TRAINING_MODE && numAgentsReceived == NUM_AGENT) ||
			(this.mode == GameSetting.PRODUCTION_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL)) 
		{
			double[][] avgPrices = new double[NUM_ITERATION][NUM_GOODS];
			double[] maxDists = new double[NUM_ITERATION];
			
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				//String path = ""; // some absolute path will be needed when it deployed to nyx.
				String path = pp_path + GameSetting.GAME_TYPE + "/" + GameSetting.DIST_TYPE + "/";
				for (Strategy strat : strategies)
				{
					String filename = strat.getPPName()  + "_N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
					File f = new File(path + filename);
					if (!f.exists())
					{
						System.out.println("Prediction data for strategy does not exist!");
						System.exit(-1);
					}
					else
					{
						try
						{
							CsvReader cr = new CsvReader(path + filename);
							
							if (strat.getPredictionType() == POINT)
							{
								double[] pp = new double[NUM_GOODS];
								cr.readRecord();
								for (int i=0;i<NUM_GOODS;i++)
								{
									pp[i] = Double.parseDouble(cr.get(i));
								}
								cr.close();
								strat.setPricePrediction(pp);
							}
							else if (strat.getPredictionType() == DISTRIBUTION)
							{
								double[][] pp = new double[NUM_GOODS][VALUE_UPPER_BOUND+1];
								for (int i=0;i<NUM_GOODS;i++)
								{
									cr.readRecord();
									for (int k=0;k<VALUE_UPPER_BOUND+1;k++)
									{
										pp[i][k] = Double.parseDouble(cr.get(k));
										//System.out.println("!!!!!!!!!: " + pp[i][k]);
									}
								}
								cr.close();
								strat.setPricePrediction(pp);
							}
							else if (strat.getPredictionType() == DISTRIBUTION_MIX)
							{
								double[][][] pp = new double[NUM_DIST_MIX][NUM_GOODS][VALUE_UPPER_BOUND+1];
								for (int i=0;i<NUM_DIST_MIX;i++)
								{
									for (int j=0;j<NUM_GOODS;j++)
									{
										cr.readRecord();
										for (int k=0;k<VALUE_UPPER_BOUND+1;k++)
										{
											pp[i][j][k] = Double.parseDouble(cr.get(k));
											//System.out.println("!!!!!!!!!: " + pp[i][k]);
										}
									}
								}
								cr.close();
								strat.setPricePrediction(pp);
							}
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			
			for (int j=0;j<NUM_ITERATION;j++)
			{
				double[] prevAvg = new double[NUM_GOODS];
				for (int k=0;k<avgPrice.length;k++)
				{
					prevAvg[k] = avgPrice[k];
				}
				//pp = prevAvg;
				
				/*
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}
				*/
				if (ENABLE_ANALYZER) analyzer = new SCPPAnalyzer();
				avgPrice = new double[NUM_GOODS];
				sumOptValue = 0.0;
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					if (GameSetting.DIST_TYPE.equalsIgnoreCase("original"))
						this.initTypeDistOriginal(); // Distribution setting from the paper
					//else if (GameSetting.DIST_TYPE.equalsIgnoreCase("bayesian"))
						//this.initTypeDistBayesian();
					//else if (GameSetting.DIST_TYPE.equalsIgnoreCase("shuffle"))
						//this.initTypeDistShuffle();
					else 
					{
						System.out.println("Invalid Distribution Option!");
						System.exit(-1);
					}
					/*
					double[] pp = {14.5,17.0,10.7,8.45,4.33};
					for (Strategy st : strategies)
					{
						st.setPricePrediction(pp);
					}
					*/
					//this.initTest();
					//System.out.println(i + " th simulation running..");
					run();
				}
				if (this.mode == GameSetting.PRODUCTION_MODE)
				{
					if (j == GameSetting.NUM_ITERATION - 1) writeResult(true, j);
					else writeResult(false, j);
					//writeValueResult();
					/*
					System.out.println("Average Utility");
					for (Strategy strat : strategies)
					{
						System.out.print(strat.getAverageUtility() + " ");
						strat.resetObservation();
					}
					System.out.println();
					*/
					double sumAvgValue = 0;
					double avgOptValue = sumOptValue / NUM_SIMULATION;
					for (Strategy st : strategies)
					{
						sumAvgValue += st.getAverageValue();
						st.resetObservation();
					}
					//double avgValue = sumAvgValue / (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL);
					//System.out.println("AvgOpt Total = " + avgOptValue + ", AvgVal Total = " + sumAvgValue);
					//System.out.println("Efficiency = " + (sumAvgValue / avgOptValue * 100) + "%");
					System.out.println(sumAvgValue / avgOptValue * 100);
				}
				else if (this.mode == GameSetting.TRAINING_MODE)
				{
					updatePricePrediction(j);
					//if (j>2 && Math.abs(maxDists[j-1]) > Math.abs(maxDists[j-2]))
					//{
					//	updatePricePrediction(false, j);
					//}
					//else updatePricePrediction(true, j);
				}
				/*
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}*/
				
				double max_dist = getMaxDist();
				/*
				for (int i=0;i<NUM_GOODS;i++)
				{
					avgPrice[i] = avgPrice[i]/(double)NUM_SIMULATION;
					if (Math.abs(avgPrice[i] - prevAvg[i]) > Math.abs(max_dist))
					{
						max_dist = avgPrice[i] - prevAvg[i];
					}
					//System.out.print(avgPrice[i] + " ");
				}*/
				//NumberFormat f = new DecimalFormat("###.#######");
				if (PRINT_OUTPUT) System.out.print("avg price after simulation: ");
				for (int i=0;i<NUM_GOODS;i++)
				{
					avgPrice[i] = avgPrice[i]/(double)NUM_SIMULATION;
					if (PRINT_OUTPUT) System.out.print(avgPrice[i] + " ");
					avgPrices[j][i] = avgPrice[i];
				}
				if (PRINT_OUTPUT)
				{
					System.out.println();
					System.out.println(max_dist);
				}
				maxDists[j] = max_dist;
				//System.out.println("Distance between prediction and actual prices = " + this.getAccDist());
				// Write out prediction as CSV file when its distance goes below threshold.
				if (this.mode == GameSetting.TRAINING_MODE)
				{
					Strategy strat = strategies.get(0); // Assuming all strategies are same, get the first strategy
					String path = pp_output_path + GameSetting.GAME_TYPE + "/" + GameSetting.DIST_TYPE + "/";
					String filename = strat.getName()  + "_N" + NUM_AGENT + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND + ".csv";
					File fPath = new File(path);
					if (!fPath.exists()) fPath.mkdirs();
					if (strat.getPredictionType() == GameSetting.POINT && Math.abs(max_dist/(double)VALUE_UPPER_BOUND) < GameSetting.MIN_POINT_DIST_TO_TERMINATE)
					{
						CsvWriter cw = new CsvWriter(path + filename);
						
						double[] pp = strat.<double[]>getPricePrediction();
						try
						{
							for (int i=0;i<NUM_GOODS;i++)
							{
								cw.write(Double.toString(pp[i]));
								//cw.write(Double.toString(avgPrice[i]));
							}
							cw.endRecord();
							cw.flush();
							cw.close();
							break;
							//System.exit(0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (strat.getPredictionType() == GameSetting.DISTRIBUTION && Math.abs(max_dist) < GameSetting.MIN_DISTRIBUTION_DIST_TO_TERMINATE)
					{
						CsvWriter cw = new CsvWriter(path + filename);
						
						double[][] pp = strat.<double[][]>getPricePrediction();
						try
						{
							for (int i=0;i<NUM_GOODS;i++)
							{
								for (int k=0;k<VALUE_UPPER_BOUND+1;k++)
								{
									cw.write(Double.toString(pp[i][k]));
								}
								cw.endRecord();
							}
							cw.endRecord();
							cw.flush();
							cw.close();
							System.exit(0);
							break;
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
			}
			
			if (ENABLE_ANALYZER) analyzer.printUtilityStatistics();
			if (PRINT_OUTPUT)
			{
				for (int j=0;j<NUM_ITERATION;j++)
				{
					for (int i=0;i<NUM_GOODS;i++)
					{
						System.out.print(avgPrices[j][i] + " ");
					}
					System.out.println();
				}
				System.out.println();
				for (int j=0;j<NUM_ITERATION;j++)
				{
					System.out.println(maxDists[j]);
				}
				System.out.println();
				System.out.print("std. dev. of payoffs are: ");
				for (int i=0;i<NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;i++)
				{
					this.averagePayoff[i] = this.averagePayoff[i] / (double)NUM_ITERATION; 
					double SS = 0.0;
					for (int j=0;j<NUM_ITERATION;j++)
					{
						SS += Math.pow((double)payoffRecord[i][j] - averagePayoff[i], 2.0);
					}
					double sampleVariance = SS / (double)(NUM_ITERATION - 1);
					double stddev = Math.sqrt(sampleVariance);
					System.out.print(stddev + " ");
				}
				System.out.println();
				System.out.println("Payoff records:");
				for (int i=0;i<NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;i++)
				{
					for (int j=0;j<NUM_ITERATION;j++)
					{
						System.out.print(this.payoffRecord[i][j] + " ");
					}
					System.out.println();
				}
			}
			
			/*
			System.out.println("Average Valuation");
			for (int j=0;j<NUM_GOODS;j++)
			{
				System.out.print((double)this.sumValue[j] / (double)this.distCount + " ");
			}
			System.out.println();
			*/
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				payoff_out.flush();
				payoff_out.close();
			}
			System.exit(0);
		}
	}
	
	
	/*
	public void register(Strategy s)
	{
		if (numAgentsReceived == NUM_AGENT) return;
		// get Strategy object here
		
		// temporary setting of initial dist
		//this.initPricePrediction(s);
				
		strategies.add(s);
		System.out.println("Strategy added with index = " + s.getIndex());
		numAgentsReceived++;
		if (numAgentsReceived == NUM_AGENT) 
		{
			double[][] avgPrices = new double[NUM_ITERATION][NUM_GOODS];
			double[] maxDists = new double[NUM_ITERATION];
			for (int j=0;j<NUM_ITERATION;j++)
			{
				double[] prevAvg = new double[NUM_GOODS];
				for (int k=0;k<avgPrice.length;k++)
				{
					prevAvg[k] = avgPrice[k];
				}
				//pp = prevAvg;
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}
				
				avgPrice = new double[NUM_GOODS];
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					initTypeDistOriginal();
					//System.out.println(i + " th simulation running..");
					run();
				}
				updatePricePrediction();
				
				for (Strategy st : strategies)
				{
					st.printPrediction();
				}
				
				double max_dist = getMaxDist();
				
				System.out.print("avg price after simulation: ");
				for (int i=0;i<NUM_GOODS;i++)
				{
					avgPrice[i] = avgPrice[i]/(double)NUM_SIMULATION;
					System.out.print(avgPrice[i] + " ");
					avgPrices[j][i] = avgPrice[i];
				}
				System.out.println();
				System.out.println(max_dist);
				maxDists[j] = max_dist;
			}
			for (int j=0;j<NUM_ITERATION;j++)
			{
				for (int i=0;i<NUM_GOODS;i++)
				{
					System.out.print(avgPrices[j][i] + " ");
				}
				System.out.println();
			}
			System.out.println();
			for (int j=0;j<NUM_ITERATION;j++)
			{
				System.out.println(maxDists[j]);
			}
			System.exit(0);
		}
	}
	*/
	private double getMaxDist()
	{
		double max_dist = 0;
		for (Strategy s : strategies)
		{
			if (Math.abs(s.getMaxDist()) > Math.abs(max_dist))
			{
				max_dist = s.getMaxDist();
			}
		}
		return max_dist;
	}
	
	private void updatePricePrediction(int currentIt)
	{
		for (Strategy s : strategies)
		{
			s.setNewPredictionAverage(currentIt);
		}
	}
	
	private void initTypeDistOriginal()
	{
		Random ran = new Random();
		int numSets = (int)Math.pow(2, NUM_GOODS);
		double[] values = new double[NUM_AGENT *
		                               HIERARCHICAL_REDUCTION_LEVEL * numSets];
		// single-unit demand for agent_1
		//System.out.println(v_one + " " + v_i_upper_bound);
		Iterator<Strategy> iter = strategies.iterator();
		int coeff_count = 0;
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int jobLength = 1 + ran.nextInt(NUM_GOODS);
			int[] deadlineValues = new int[NUM_GOODS];
			for (int i=0;i<NUM_GOODS;i++)
			{
				deadlineValues[i] = i < jobLength - 1 ? 0 : 1 + ran.nextInt(VALUE_UPPER_BOUND);
			}
			// Need to ensure monotonicity
			for (int i=jobLength-1;i<NUM_GOODS-1;i++)
			{
				if (deadlineValues[i] < deadlineValues[i+1])
				{
					boolean foundLesserVal = false;
					for (int j=i+2;j<NUM_GOODS && !foundLesserVal;j++)
					{
						if (deadlineValues[j] <= deadlineValues[i])
						{
							deadlineValues[i+1] = deadlineValues[j];
							foundLesserVal = true;
						}
					}
					if (!foundLesserVal) deadlineValues[i+1] = 0;
				}
			}
			
			if (PRINT_DEBUG)
			{
				System.out.println("Deadline values for agent " + s.getIndex() + " with job length = " + jobLength);
				for (int i=0;i<NUM_GOODS;i++)
				{
					System.out.print(deadlineValues[i] + " ");
				}
				System.out.println();
			}
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			// Give type distribution for every possible set of goods
			for (BitSet bs : bitVector)
			{
				if (bs.cardinality() < jobLength) 
				{
					typeDist.put(bs, new Integer(0));
					values[coeff_count] = 0;
				}
				else
				{
					int count = 0;
					int deadline = 0;
					for (int i=0;i<bs.length();i++)
					{
						if (bs.get(i)) {
							count++;
						}
						if (count == jobLength) {
							deadline = i;
							break;
						}
					}
					typeDist.put(bs, new Integer(deadlineValues[deadline]));
					values[coeff_count] = deadlineValues[deadline];
				}
				coeff_count++;
			}
			s.setTypeDist(typeDist);
		} // end while
		
		/*
		System.out.println("values");
		for (int i=0;i<values.length;i++)
		{
			System.out.print(values[i] + " ");
			if ((i+1)%numSets == 0) System.out.println();
		}
		*/
		
		////////////////////////////////////////////////////////////////////////
		// value allocation optimization
		////////////////////////////////////////////////////////////////////////
		
		
		int numVar = numSets * NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;
		
		try
		{
			int[] colNo = new int[numSets];
			double[] row = new double[numSets];
			LpSolve solver = LpSolve.makeLp(0, numVar);
			solver.setObjFn(values);
			solver.setVerbose(0);
			solver.setMaxim();
			for (int i=1;i<=numVar;i++)
			{
				solver.setBinary(i, true);
			}
			double[] constraint = new double[numVar];
			for (int i=0;i<NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;i++)
			{
				int count = 0;
				constraint = new double[numVar];
				colNo = new int[numSets];
				row = new double[numSets];
				for (int j=1;j<numSets;j++)
				{
					constraint[i * numSets + j] = 1;
					//colNo[count] = 1 + i * numSets + j;
					//row[count] = 1;
					//count++;
				}
				solver.addConstraint(constraint, LpSolve.LE, 1);
				//solver.addConstraintex(count, row, colNo, LpSolve.LE, 1);
			}
			for (int j=1;j<numSets;j++)
			{
				int count = 0;
				constraint = new double[numVar];
				colNo = new int[numSets];
				row = new double[numSets];
				for (int i=0;i<NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;i++)
				{
					//colNo[count] = 1 + i * numSets + j;
					//row[count] = 1;
					//count++;
					constraint[i * numSets + j]  = 1;
				}
				solver.addConstraint(constraint, LpSolve.LE, 1);
				//solver.addConstraintex(count, row, colNo, LpSolve.LE, 1);
			}
			
			int bitLen = 0;
			int count = 0;
		
			for (int i=0;i<NUM_GOODS;i++)
			{
				colNo = new int[numSets * NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL];
				row = new double[numSets* NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL];
				constraint = new double[numVar];
				count = 0;
				for (int j=0;j<bitVector.length;j++)
				{
					if (bitVector[j].get(i))
					{
						for (int k=0;k<NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;k++)
						{
							constraint[k * numSets + j] = 1;
							//colNo[count] = 1 + k * numSets + j;
							//row[count] = 1;
							//count++;
						}
					}
				}
				solver.addConstraint(constraint, LpSolve.LE, 1);
				//solver.addConstraintex(count, row, colNo, LpSolve.LE, 1);
			}
			solver.setDebug(true);
			//solver.addConstraint(constraint, LpSolve.EQ, numSets - 1);
			solver.solve();
			//double[] var = solver.getPtrVariables();
			//double[] consts = solver.getPtrConstraints();
			//System.out.println("Value of objective function: " + solver.getObjective());
			sumOptValue += solver.getObjective();
			//double sum = 0;
	
			solver.deleteLp();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		////////////////////////////////////////////////////////////////////////
		////////////////////////////////////////////////////////////////////////	
		
		if (PRINT_DEBUG)
		{
			iter = strategies.iterator();
			while (iter.hasNext())
			{
				Strategy s = iter.next();
				System.out.println("Agent " + s.getIndex() + "'s type dist:");
				if (s.isSingleUnitDemand()) System.out.println("Single-unit demand.");
				else System.out.println("Non single-unit demand.");
				Map<BitSet, Integer> m = s.getTypeDist();
				
				for (BitSet bs : bitVector)
				{
					int value = m.get(bs).intValue();
					System.out.println(bs + " " + value + " " + bs.cardinality());
				}
			}
		}
	}
	
	
	private void initTypeDist()
	{
		Random ran = new Random();
		// single-unit demand for agent_1
		int v_one = 3 + ran.nextInt(VALUE_UPPER_BOUND - 3);
		int v_i_upper_bound = ((NUM_GOODS * (v_one-1)) < VALUE_UPPER_BOUND) ? (NUM_GOODS * (v_one-1)) : VALUE_UPPER_BOUND;
		//System.out.println(v_one + " " + v_i_upper_bound);
		Iterator<Strategy> iter = strategies.iterator();
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			if (s.getIndex() == 1)
			{
				
				// Give type distribution for every possible set of goods
				for (BitSet bs : bitVector)
				{
					//System.out.println(bs);
					if (bs.cardinality() >= 1)
					{
						typeDist.put(bs, new Integer(v_one));
					}
					else typeDist.put(bs, new Integer(0));
				}
			}
			else
			{
				int v_i = v_one + 1 + ran.nextInt(v_i_upper_bound - v_one);
				// Give type distribution for every possible set of goods
				for (BitSet bs : bitVector)
				{
					/*
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
					}*/
					if (bs.cardinality() == NUM_GOODS)
					{
						typeDist.put(bs, new Integer(v_i));
					}
					else typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
			//initPricePrediction(s);
			//tempSts.add(s);
		}
		//strategies = tempSts;
		
		/*
		iter = strategies.iterator();
		while (iter.hasNext())
		{
			Strategy s = iter.next();
			System.out.println("Agent " + s.getIndex() + "'s type dist:");
			if (s.isSingleUnitDemand()) System.out.println("Single-unit demand.");
			else System.out.println("Non single-unit demand.");
			Map<BitSet, Integer> m = s.getTypeDist();
			
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
				int value = m.get(bs).intValue();
				
				System.out.println(bs + " " + value);
			}
		}
		*/
	}
	private void initPricePrediction(Strategy s)
	{
		// initial prediction of strategies are zeros
		/*
		int[] newP = new int[NUM_GOODS];
		Random r = new Random();
		for (int i=0;i<NUM_GOODS;i++)
		{
			newP[i] = r.nextInt(47) + 3;
		}
		s.setPricePrediction(newP);*/
	}
	private void run()
	{
		int numAgent = NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;
		//ArrayList<int[]> bids = new ArrayList<int[]>();
		double[][] bids = new double[numAgent][NUM_GOODS];
		// testing a single SAA
		boolean isQuiescent = false;
		
		InformationState state = new InformationState(NUM_GOODS);
		while (!isQuiescent)
		{
			// Supply current information state to users
			double[] currentBids = state.getCurrentBidPrice();
			int[] currentWinning = state.getCurrentBidWinning();
			/*
			System.out.print("Current prices: ");
			for (int i=0;i<NUM_GOODS;i++)
			{
				System.out.print(currentBids[i] + " ");
			}
			System.out.println();
			*/
			for (int i=0;i<numAgent;i++)
			{
				Strategy s = strategies.get(i);
				double[] newbid = s.bid(state);
				
				//System.out.print("Agent " + s.getIndex() + " bids: ");
				for (int j=0;j<NUM_GOODS;j++)
				{
					//System.out.print(newbid[j] + " ");
					bids[i][j] = newbid[j];
				}
				//System.out.println();
			}
			isQuiescent = true;
			for (int i=0;i<NUM_GOODS;i++)
			{
				double max = currentBids[i];
				//int agentWon = 0;
				for (int j=0; j<numAgent;j++)
				{
					if (bids[j][i] > max)
					{
						max = bids[j][i];
						//agentWon = j;
					}
				}
				if (currentBids[i] < max)
				{
					currentBids[i] = max;
					ArrayList<Integer> winners = new ArrayList<Integer>();
					int winner = 0;
					for (int j=0;j<numAgent;j++)
					{
						if (bids[j][i] == max)
						{
							winners.add(new Integer(strategies.get(j).getIndex()));
						}
					}
					// Random tie-break
					if (winners.size() > 1)
					{
						Random r = new Random();
						winner = winners.get(r.nextInt(winners.size())).intValue();
					}
					else winner = winners.get(0).intValue();
					currentWinning[i] = winner;
					isQuiescent = false;
				}
			}
			state.setBidPrice(currentBids);
			state.setBidWinning(currentWinning);
			
			/*
			System.out.print("Agent currently winning: ");
			for (int i=0;i<NUM_GOODS;i++)
			{
				System.out.print(currentWinning[i] + " ");
			}
			System.out.println();
			System.out.println();
			*/
		} // end while
		
		for (Strategy s : strategies)
		{
			s.addObservation(state);
		}
		
		double[] currentBids = state.getCurrentBidPrice();
		int[] currentWinning = state.getCurrentBidWinning();
		for (int i=0;i<NUM_GOODS;i++)
		{
			avgPrice[i] += currentBids[i];
		}
		
		/*
		System.out.print("Final prices: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentBids[i] + " ");
		}
		System.out.println();
		System.out.print("Agent who won the good: ");
		for (int i=0;i<NUM_GOODS;i++)
		{
			System.out.print(currentWinning[i] + " ");
		}
		System.out.println();
		System.out.println("Utility of each agent: ");
		for (int i=0;i<NUM_AGENT;i++)
		{
			Strategy s = strategies.get(i);
			System.out.print(s.getCurrentSurplus(state) + " ");
		}
		System.out.println();*/
		//this.numAgentsReceived = 0;
		//strategies.clear();
	}
	
	private void writeResult(boolean lastIteration, int currentIt)
	{
		Map<String, Object> payoffMap = new HashMap<String, Object>(); // Average payoff for a strategy
		Map<String, Integer> countMap = new HashMap<String, Integer>(); // Count # of agents with same strategy
		
		ArrayList<String> namespace = new ArrayList<String>();
		
		for (Strategy s : strategies)
		{
			String name = s.getName();
			double payoff = s.getAverageUtility();
			
			this.payoffRecord[s.getIndex()-1][currentIt] = payoff;
			this.averagePayoff[s.getIndex()-1] += payoff;
			
			if (!namespace.contains(name))
			{
				namespace.add(name);
			}
			
			if (payoffMap.containsKey(name))
			{
				double p = Double.parseDouble(payoffMap.get(name).toString());
				int count = Integer.parseInt(countMap.get(name).toString());
				
				p = p + payoff;
				count++;
				
				payoffMap.put(name, p);
				countMap.put(name, count);
			}
			else
			{
				payoffMap.put(name, payoff);
				countMap.put(name, 1);
			}
			
		}
		
		for (String s : namespace)
		{
			double p = Double.parseDouble(payoffMap.get(s).toString());
			int count = Integer.parseInt(countMap.get(s).toString());
			
			p = p / (double)count;
			
			payoffMap.put(s, p);
		}
		
		StringWriter w = new StringWriter();
		DumperOptions options = new DumperOptions();
		options.setExplicitStart(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		//if (lastIteration) options.setExplicitEnd(true);
		//else options.setExplicitEnd(false);
		options.setExplicitEnd(false);
		Yaml y = new Yaml(options);
		y.dump(payoffMap, w);
		payoff_out.print(w.toString());	
	}
}
