package org.srg.scpp_im.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import org.yaml.snakeyaml.Yaml;

public class ProfileTrainer extends SelfContainedGameRunner
{
	private static int strCount = 0;
	private static ArrayList<int []> combinations = new ArrayList<int []>();
	// Override
	public static void main(String[] args)
	{
		mode = "training";
		GameSetting.SIMUL_PATH = args[0];
		parseSimulationSpecYAML(SIMUL_PATH + "/simulation_spec.yaml");
		GameSetting.PROFILE_BASED = true;
		Set<String> strSet = new HashSet<String>();
				
		for (String s : strNames)
		{
			if (strSet.add(s)) strCount++;
		}
		Object[] arr = strSet.toArray();
		String[] names = new String[strCount];
		for (int i=0;i<arr.length;i++)
		{
			names[i] = (String)arr[i];
		}
		getCombinations();		
		System.out.println("strCount = " + strCount);
		System.out.println("Combinations size = " + combinations.size());
		
		
		for (int i=0;i<combinations.size();i++)
		{
			int count = 0;
			strategies = new ArrayList<Strategy>();
			int[] comb = combinations.get(i);
			//System.out.println(comb.length);
			try
			{
				for (int j=0;j<comb.length;j++)
				{
					for (int k=0;k<comb[j];k++)
					{
						String strategyName = "org.srg.scpp_im.strategy." + 
							names[j];
						System.out.println(strategyName);
						Class agentClass = Class.forName(strategyName);
						Constructor con = agentClass.getConstructor(int.class);
						
						for (int h=0;h<HIERARCHICAL_REDUCTION_LEVEL;h++)
						{
							Strategy s = (Strategy)con.newInstance(count + 1);
							strategies.add(s);
							count++;
						}
					}
				}
				
				Class serverClass = Class.forName("org.srg.scpp_im.game." + game);
				Constructor serverCon = serverClass.getConstructor(int.class);
				Register r;
	
				r = (Register)serverCon.newInstance(GameSetting.TRAINING_MODE);
				
		        for (Strategy s : strategies)
		        {
		        	r.register(s);
		        }
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
		
	protected static void parseSimulationSpecYAML(String file)
	{
		try
		{
			InputStream input = new FileInputStream(new File(file));
			Yaml yaml = new Yaml();
			
			Iterator<Object> spec = yaml.loadAll(input).iterator();
			strNames = (List<String>)spec.next();
			LinkedHashMap config = (LinkedHashMap)spec.next();
			
			if (!numAgentSpecified) GameSetting.NUM_AGENT = (int)Double.parseDouble(config.get("num agents").toString());
			
			GameSetting.NUM_GOODS = (int)Double.parseDouble(config.get("num goods").toString());
			assert GameSetting.NUM_GOODS > 0;
			GameSetting.NUM_ITERATION = (int)Double.parseDouble(config.get("num iterations").toString());
			assert GameSetting.NUM_ITERATION > 0;
			GameSetting.NUM_SIMULATION = (int)Double.parseDouble(config.get("num simulations").toString());
			assert GameSetting.NUM_SIMULATION > 0;
			GameSetting.NUM_SAMPLE = (int)Double.parseDouble(config.get("num sample").toString());
			assert GameSetting.NUM_SAMPLE > 0;
			GameSetting.NUM_SCENARIO = (int)Double.parseDouble(config.get("num scenario").toString());
			assert GameSetting.NUM_SCENARIO > 0;
			GameSetting.NUM_CANDIDATE_BID = (int)Double.parseDouble(config.get("num candidate bid").toString());
			assert GameSetting.NUM_CANDIDATE_BID > 0;
			//GameSetting.NUM_DIST_MIX = (int)Double.parseDouble(config.get("num dist mix").toString());
			//assert GameSetting.NUM_DIST_MIX > 0;
			GameSetting.PRINT_DEBUG = Boolean.parseBoolean(config.get("print debug").toString());
			GameSetting.PRINT_OUTPUT = Boolean.parseBoolean(config.get("print output").toString());
			GameSetting.PROFILE_BASED = Boolean.parseBoolean(config.get("profile based").toString());
			//GameSetting.ENABLE_ANALYZER = Boolean.parseBoolean(config.get("enable analyzer").toString());
			GameSetting.UPDATE_THRESHOLD = Double.parseDouble(config.get("update threshold").toString());
			GameSetting.VALUE_UPPER_BOUND = (int)Double.parseDouble(config.get("value upper bound").toString());
			assert GameSetting.VALUE_UPPER_BOUND > 0;
			GameSetting.HIERARCHICAL_REDUCTION_LEVEL = (int)Double.parseDouble(config.get("hierarchical reduction level").toString());
			assert GameSetting.HIERARCHICAL_REDUCTION_LEVEL > 0;
			GameSetting.MIN_POINT_DIST_TO_TERMINATE = Double.parseDouble(config.get("min point dist to terminate").toString());
			assert GameSetting.MIN_POINT_DIST_TO_TERMINATE > 0;
			GameSetting.MIN_DISTRIBUTION_DIST_TO_TERMINATE = Double.parseDouble(config.get("min distribution dist to terminate").toString());
			assert GameSetting.MIN_DISTRIBUTION_DIST_TO_TERMINATE > 0;
			GameSetting.DIST_TYPE = config.get("dist type").toString();
			game = config.get("game type").toString();
			GameSetting.GAME_TYPE = game;
			assert !GameSetting.GAME_TYPE.isEmpty();
			assert !GameSetting.DIST_TYPE.isEmpty();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}	
	
	private static void getCombinations()
	{
		for (int i=0;i<=NUM_AGENT;i++)
		{
			int[] combination = new int[strCount];
			combination[0] = i;
			getCombination(combination, 1, i);
//			if (comb != null) list.add(comb);
		}
//		return list;
	}
	private static void getCombination(int[] comb, int n, int sum)
	{
	/*
		for (int j=0;j<comb.length;j++)
		{
			System.out.print(comb[j] + " ");
		}
		System.out.println(" : " + n + " : " + sum);*/
		if (n == strCount && sum == NUM_AGENT) 
		{
			int[] newComb = new int[strCount];
			System.arraycopy(comb, 0, newComb, 0, strCount);
			combinations.add(newComb);
		}
		else if (n == strCount) return;
		else
		{
			for (int i=0;i<=NUM_AGENT-comb[n-1];i++)
			{
				comb[n] = i;
							
				getCombination(comb, n+1, sum+i);
//				getCombination(comb, n-1, c, sum);
			}	
		}		
//		return null;
	}
}
