package org.srg.scpp_im.game;

import com.csvreader.*;
import org.yaml.snakeyaml.*;
import java.io.File;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.io.StringWriter;

public class SecondPriceOneShotMarketScheduling extends SecondPriceOneShotGame {

	public SecondPriceOneShotMarketScheduling()
	{
		super();
	}
	
	public SecondPriceOneShotMarketScheduling(int mode)
	{
		super();
		this.mode = mode;
	}
	
	public void register(Strategy s)
	{
		if (numAgentsReceived == NUM_AGENT) return;
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
		if (numAgentsReceived == NUM_AGENT) 
		{
			double[][] avgPrices = new double[NUM_ITERATION][NUM_GOODS];
			double[] maxDists = new double[NUM_ITERATION];
			
			if (this.mode == GameSetting.PRODUCTION_MODE)
			{
				//String path = ""; // some absolute path will be needed when it deployed to nyx.
				String path = pp_path + GameSetting.GAME_TYPE + "/Bayesian/";
				for (Strategy strat : strategies)
				{
					String filename = strat.getName()  + "_N" + NUM_AGENT + "M" + NUM_GOODS + ".csv";
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
							CsvReader cr = new CsvReader(pp_path + filename);
							
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
								int[][] pp = new int[NUM_GOODS][VALUE_UPPER_BOUND+1];
								for (int i=0;i<NUM_GOODS;i++)
								{
									cr.readRecord();
									for (int k=0;k<VALUE_UPPER_BOUND+1;k++)
									{
										pp[i][k] = Integer.parseInt(cr.get(k));
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
				
				avgPrice = new double[NUM_GOODS];
				for (int i=0;i<NUM_SIMULATION;i++)
				{
					this.initTypeDistBayesian();
					//this.initTypeDistShuffle();
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
					if (j == GameSetting.NUM_ITERATION - 1) writeResult(true);
					else writeResult(false);
					/*
					System.out.println("Average Utility");
					for (Strategy strat : strategies)
					{
						System.out.print(strat.getAverageUtility() + " ");
						strat.resetObservation();
					}
					System.out.println();
					*/
				}
				else if (this.mode == GameSetting.TRAINING_MODE)
				{
					if (j>2 && Math.abs(maxDists[j-1]) >= Math.abs(maxDists[j-2]))
					{
						updatePricePrediction(false);
					}
					else updatePricePrediction(true);
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
				
				// Write out prediction as CSV file when its distance goes below threshold.
				if (this.mode == GameSetting.TRAINING_MODE)
				{
					Strategy strat = strategies.get(0); // Assuming all strategies are same, get the first strategy
					String path = pp_path + GameSetting.GAME_TYPE + "/Bayesian/";
					String filename = strat.getName()  + "_N" + NUM_AGENT + "M" + NUM_GOODS + ".csv";
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
							}
							cw.endRecord();
							cw.flush();
							cw.close();
							System.exit(0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
					else if (strat.getPredictionType() == GameSetting.DISTRIBUTION && Math.abs(max_dist/(double)VALUE_UPPER_BOUND) < GameSetting.MIN_DISTRIBUTION_DIST_TO_TERMINATE)
					{
						CsvWriter cw = new CsvWriter(path + filename);
						
						int[][] pp = strat.<int[][]>getPricePrediction();
						try
						{
							for (int i=0;i<NUM_GOODS;i++)
							{
								for (int k=0;k<VALUE_UPPER_BOUND+1;k++)
								{
									cw.write(Integer.toString(pp[i][k]));
								}
								cw.endRecord();
							}
							cw.endRecord();
							cw.flush();
							cw.close();
							System.exit(0);
						}
						catch(Exception e)
						{
							e.printStackTrace();
						}
					}
				}
				
			}
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
	
	private void writeResult(boolean lastIteration)
	{
		Map<String, Object> payoffMap = new HashMap<String, Object>(); // Average payoff for a strategy
		Map<String, Integer> countMap = new HashMap<String, Integer>(); // Count # of agents with same strategy
		
		ArrayList<String> namespace = new ArrayList<String>();
		
		for (Strategy s : strategies)
		{
			String name = s.getName();
			double payoff = s.getAverageUtility();
			
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
		if (lastIteration) options.setExplicitEnd(true);
		else options.setExplicitEnd(false);
		Yaml y = new Yaml(options);
		y.dump(payoffMap, w);
		payoff_out.print(w.toString());	
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
