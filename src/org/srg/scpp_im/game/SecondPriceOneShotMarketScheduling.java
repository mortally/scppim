package org.srg.scpp_im.game;

import com.csvreader.*;

import org.srg.scpp_im.analysis.SCPPAnalyzer;
import org.srg.scpp_im.analysis.UtilAnalyzer;
import org.srg.scpp_im.strategy.OSSCDP_BidEvaluatorSMU_E64S256K4;
import org.yaml.snakeyaml.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.io.StringWriter;

import	lpsolve.*;

public class SecondPriceOneShotMarketScheduling extends SecondPriceOneShotGame {

	public SecondPriceOneShotMarketScheduling()
	{
		super();
	}
	
	public SecondPriceOneShotMarketScheduling(int mode)
	{
		super();
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
		if (this.mode == GameSetting.TRAINING_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) return;
		if (this.mode == GameSetting.PRODUCTION_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) return;
		// get Strategy object here
		
		// temporary setting of initial dist
		//this.initPricePrediction(s);
				
		strategies.add(s);
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
		if ((this.mode == GameSetting.TRAINING_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) ||
			(this.mode == GameSetting.PRODUCTION_MODE && numAgentsReceived == NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL)) 
		{
			double[][] avgPrices = new double[NUM_ITERATION][NUM_GOODS];
			double[] maxDists = new double[NUM_ITERATION];
			String profile = "";
			
			if (PROFILE_BASED)
			{
				Map<String, Integer> strategyMap = createUniqueStrategyMap();
				Set<String> strategySet = strategyMap.keySet();
				Object[] strategyObjArray = strategySet.toArray();
				String[] strategyArray = new String[strategyObjArray.length];
				
				for (int i=0;i<strategyArray.length;i++)
				{
					strategyArray[i] = (String)strategyObjArray[i];
				}
				
				Arrays.sort(strategyArray);
				
				for (int i=0;i<strategyArray.length;i++)
				{
					String name = strategyArray[i];
					int count = strategyMap.get(name).intValue();
					profile += (name + "_" + count + "_");
				}
			}
			
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				//String path = ""; // some absolute path will be needed when it deployed to nyx.
				String path = pp_path + GameSetting.GAME_TYPE + "/" + GameSetting.DIST_TYPE + "/";
				for (Strategy strat : strategies)
				{
					String filename;
					if (PROFILE_BASED) filename = profile + "N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
					else 
					{
						if (HIGHEST_BID_PREDICTION) filename = strat.getPPName()  + "_HB_N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
						else filename = strat.getPPName()  + "_N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
					}
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
				simulCount = 0;
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					if (GameSetting.DIST_TYPE.equalsIgnoreCase("original"))
						this.initTypeDistOriginal(); // Distribution setting from the paper
					else if (GameSetting.DIST_TYPE.equalsIgnoreCase("bayesian"))
						this.initTypeDistBayesian();
					else if (GameSetting.DIST_TYPE.equalsIgnoreCase("shuffle"))
						this.initTypeDistShuffle();
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
					//System.out.println(sumAvgValue / avgOptValue * 100);
					System.out.println("Candidate util = " + UtilAnalyzer.bestCandidateUtil / (NUM_SIMULATION * NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL));
				}
				else if (this.mode == GameSetting.TRAINING_MODE)
				{
					if (j>2 && Math.abs(maxDists[j-1]) > Math.abs(maxDists[j-2]))
					{
						updatePricePrediction(false, j);
					}
					else updatePricePrediction(true, j);
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
					String filename;
					if (PROFILE_BASED) filename = profile + "N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND + ".csv"; 
					else 
					{
						if (HIGHEST_BID_PREDICTION) filename = strat.getPPName()  + "_HB_N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
						else filename = strat.getPPName()  + "_N" + (NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL) + "M" + NUM_GOODS + "V" + VALUE_UPPER_BOUND +  ".csv";
					}
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
						//	System.exit(0);
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
			//System.exit(0);
		}
	}
	
	/*
	private void printVariance()
	{
		System.out.print("Variances of utilities are: ");
		for (Strategy s : strategies)
		{
			double meanUtil = s.getAverageUtility();
			int[] utilityRecord = s.getUtilityRecord();
			double SS = 0.0;
			for (int i=0;i<utilityRecord.length;i++)
			{
				SS += Math.pow((double)utilityRecord[i] - meanUtil, 2.0);
			}
			
			double sampleVariance = SS / (double)(utilityRecord.length - 1);
			System.out.print(sampleVariance + " ");
		}
		System.out.println();
	}
	*/
	
	private void writeValueResult()
	{
		Map<String, Object> valueMap = new HashMap<String, Object>(); // Average value for a strategy
		Map<String, Integer> countMap = new HashMap<String, Integer>(); // Count # of agents with same strategy
		
		ArrayList<String> namespace = new ArrayList<String>();
		
		for (Strategy s : strategies)
		{
			String name = s.getName();
			double value = s.getAverageValue();
			
			if (!namespace.contains(name))
			{
				namespace.add(name);
			}
			
			if (valueMap.containsKey(name))
			{
				double v = Double.parseDouble(valueMap.get(name).toString());
				int count = Integer.parseInt(countMap.get(name).toString());
				
				v = v + value;
				count++;
				
				valueMap.put(name, v);
				countMap.put(name, count);
			}
			else
			{
				valueMap.put(name, value);
				countMap.put(name, 1);
			}
		}
		
		for (String s : namespace)
		{
			double v = Double.parseDouble(valueMap.get(s).toString());
			int count = Integer.parseInt(countMap.get(s).toString());
			
			v = v / (double)count;
			try
			{
				//File fPath = new File(SIMUL_PATH + "features/");
				//if (!fPath.exists()) fPath.mkdirs();
				OutputStream os = new FileOutputStream(new File(SIMUL_PATH + "/features/" + s), true);
				PrintStream ps = new PrintStream(os);
				DumperOptions options = new DumperOptions();
				options.setExplicitStart(true);
				options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
				Yaml y = new Yaml(options);
				StringWriter w = new StringWriter();
				y.dump(v, w);
				ps.print(w);
				ps.flush();
				ps.close();
				//os.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
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
	
	private Map<String, Integer> createUniqueStrategyMap()
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Strategy s : strategies)
		{
			String name = s.getName();
			if (map.containsKey(name))
			{
				int count = map.get(name).intValue();
				count++;
				map.put(name, count);
			}
			else map.put(name, 1);
		}
		return map;
	}
	
	private void initTypeDistBayesian()
	{
		Random ran = new Random();
		Iterator<Strategy> iter = strategies.iterator();
		
		int[] num_slot_required = new int[NUM_AGENT];
		
		for (int i=0;i<NUM_AGENT;i++)
		{
			num_slot_required[i] = 1 + ran.nextInt(NUM_GOODS);
		}
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int idx = s.getIndex() - 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required[idx];i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required[idx];i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					// single-unit demand case
					/*
					if (bs.cardinality() >= num_slot_required[idx] && num_slot_required[idx] == 1)
					{
						int earliestSlot = bs.nextSetBit(0);
						typeDist.put(bs, new Integer(valuation[earliestSlot]));
					}*/
					if (bs.cardinality() >= num_slot_required[idx] && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required[idx]]));
					}
					else if (bs.cardinality() < num_slot_required[idx]) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
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
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
		}
	}
	
	private void initTypeDistShuffle()
	{
		Random ran = new Random();
		Iterator<Strategy> iter = strategies.iterator();
		
		int[] num_slot_required = new int[NUM_AGENT];
		
		for (int i=0;i<NUM_AGENT;i++)
		{
			num_slot_required[i] = i % NUM_GOODS + 1;
		}
		for (int i=0;i<NUM_AGENT;i++)
		{
			int ranPos = ran.nextInt(NUM_AGENT);
			
			int temp = num_slot_required[ranPos];
			num_slot_required[ranPos] = num_slot_required[i];
			num_slot_required[i] = temp;
		}
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int idx = s.getIndex() - 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required[idx];i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required[idx];i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() >= num_slot_required[idx] && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required[idx]]));
					}
					else if (bs.cardinality() < num_slot_required[idx]) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
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
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
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
		
		/*
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
		*/
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
		Iterator<Strategy> iter = strategies.iterator();
		
		while(iter.hasNext())
		{
			Strategy s = iter.next();
			int num_slot_required = s.getIndex() % NUM_GOODS + 1;
			int[] valuation = new int[NUM_GOODS];
			Map<BitSet, Integer> typeDist = new HashMap<BitSet, Integer>();
			//int value = VALUE_UPPER_BOUND - NUM_GOODS; 
			
			for (int i=0;i<=NUM_GOODS-num_slot_required;i++)
			{
				valuation[i] = (i == 0) ? NUM_GOODS + ran.nextInt(VALUE_UPPER_BOUND - NUM_GOODS + 1) : NUM_GOODS + ran.nextInt(valuation[i-1] - NUM_GOODS + 1);
			}
			
			for (int i=num_slot_required;i<=NUM_GOODS;i++)
			{
				//value = NUM_GOODS + ran.nextInt(value + 1);
				for (BitSet bs : bitVector)
				{
					if (bs.cardinality() >= num_slot_required && bs.length() == i)
					{
						typeDist.put(bs, new Integer(valuation[i-num_slot_required]));
					}
					else if (bs.cardinality() < num_slot_required) 
						typeDist.put(bs, new Integer(0));
				}
			}
			s.setTypeDist(typeDist);
		}
		
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
					System.out.println(bs + " " + value + " " + bs.cardinality() + " " + bs.length());
				}
			}
		}
	}
	
}
