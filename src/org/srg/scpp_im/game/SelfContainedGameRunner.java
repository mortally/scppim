package org.srg.scpp_im.game;

import org.srg.scpp_im.strategy.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.List;
import java.util.LinkedHashMap;
import java.io.*;

//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
import org.yaml.snakeyaml.*;

import lpsolve.*;

public class SelfContainedGameRunner extends GameSetting {
	
	//protected static Document dom;
	protected static String game;
	protected static String stratToTrain = "";
	protected static String stratToProduce = "";
	protected static boolean numAgentSpecified = false;
	protected static ArrayList<Strategy> strategies;
	protected static List<String> strNames;
	protected static String mode;
	protected static double[][] avgPrices;
	protected static double[] avgPrice;
	protected static BitSet[] bitVector;
	
	public static void main(String[] args)
	{
		GameSetting.SIMUL_PATH = args[0];
		mode = args.length > 1 ? args[1] : ""; 
		
		if (mode.equalsIgnoreCase("training") && args.length > 2)
		{
			try
			{
				stratToTrain = args[2];
				numAgentSpecified = true;
				GameSetting.NUM_AGENT = Integer.parseInt(args[3]); // This meaning that when there are more than 2 args
				assert GameSetting.NUM_AGENT > 0;                 // User has to specified both strategy to be trained and # of agents
			}
			catch (Exception e)
			{
				e.printStackTrace();
				System.out.println("Wrong number/format of arguments");
			}
		}
		
		/*
		if (mode.equalsIgnoreCase("training") && args.length > 3)
		{
			
		}
		*/
		strategies = new ArrayList<Strategy>();
		
		if (!mode.equalsIgnoreCase("training") && args.length > 3) {
			stratToProduce = args[3];
		}
		
		parseSimulationSpecYAML(SIMUL_PATH + "/simulation_spec.yaml");
		//parseGameSettingXML(fileName);
		
		if (!mode.equalsIgnoreCase("training") && args.length > 2)
		{
			GameSetting.NUM_ITERATION = Integer.parseInt(args[2]);
			assert GameSetting.NUM_ITERATION > 0;
		}
		
		bitVector = new BitSet[(int)Math.pow(2,NUM_GOODS)];
		avgPrices = new double[NUM_ITERATION][NUM_GOODS];
		avgPrice = new double[NUM_GOODS];
		double[] maxDists = new double[NUM_ITERATION];
		
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
		try
		{
			Class serverClass = Class.forName("org.srg.scpp_im.game." + game);
			Constructor con;
			Register r;
			if (args.length > 1)
			{
				//mode = args[1];
				con = serverClass.getConstructor(int.class);
				if (mode.equalsIgnoreCase("training"))
				{
					r = (Register)con.newInstance(GameSetting.TRAINING_MODE);
				}
				else
				{
					r = (Register)con.newInstance(GameSetting.PRODUCTION_MODE);
				}
			}
			else
			{
				con = serverClass.getConstructor(int.class);
				r = (Register)con.newInstance(GameSetting.PRODUCTION_MODE);
			}
	        
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
			GameSetting.ENABLE_ANALYZER = Boolean.parseBoolean(config.get("enable analyzer").toString());
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
			
			if (!mode.equalsIgnoreCase("training") && GameSetting.NUM_AGENT != strNames.size())
			{
				System.out.println("Invalid YAML file: Number of agents mismatch");
		    	System.exit(-1);
			}
			
			if (mode.equalsIgnoreCase("training")) 
			{
				if (!PROFILE_BASED) GameSetting.HIERARCHICAL_REDUCTION_LEVEL = 1;
			}
			
			GameSetting.TOTAL_AGENTS = NUM_AGENT * HIERARCHICAL_REDUCTION_LEVEL;
			
			for (int i=0;i<NUM_AGENT;i++)
		    {
				String strategyName = "";
				if (!mode.equalsIgnoreCase("training")) 
				{
					if (stratToProduce.isEmpty()) strategyName = "org.srg.scpp_im.strategy." + strNames.get(i);
					else strategyName = "org.srg.scpp_im.strategy." + stratToProduce;
				}
				else if (mode.equalsIgnoreCase("training")) 
				{
					if (stratToTrain.isEmpty())	strategyName = (PROFILE_BASED) ? 
							"org.srg.scpp_im.strategy." + strNames.get(i) :
							"org.srg.scpp_im.strategy." + strNames.get(0); // only uses the first instance for training.
					else strategyName = "org.srg.scpp_im.strategy." + stratToTrain;
				}
		    	System.out.println(strategyName);

		    	Class agentClass = Class.forName(strategyName);
		    	Constructor con = agentClass.getConstructor(int.class);
		    	/*
		    	if (mode.equalsIgnoreCase("training"))
		    	{
		    		Strategy s = (Strategy)con.newInstance(i+1);
		    		strategies.add(s);
		    	}
		    	else*/ 
		    	{
		    		for (int j=0;j<HIERARCHICAL_REDUCTION_LEVEL;j++)
		    		{
		    			Strategy s = (Strategy)con.newInstance(i * HIERARCHICAL_REDUCTION_LEVEL + j + 1);
		    			strategies.add(s);
		    		}
		    	}
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	/*
	protected static void parseGameSettingXML(String file)
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		
		try
		{
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			dom = db.parse(file);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		Element root = dom.getDocumentElement();
		
		NodeList nl = root.getElementsByTagName("game");
		Element gameSetting = (Element)nl.item(0);
		
	    Element numGoods = (Element)gameSetting.getElementsByTagName("num_goods").item(0);
	    Element numAgents = (Element)gameSetting.getElementsByTagName("num_agents").item(0);
	    Element numIterations = (Element)gameSetting.getElementsByTagName("num_iterations").item(0);
	    Element numSimulations = (Element)gameSetting.getElementsByTagName("num_simulations").item(0);
	    Element valueUpperBound = (Element)gameSetting.getElementsByTagName("value_upper_bound").item(0);
	    Element gameType = (Element)gameSetting.getElementsByTagName("game_type").item(0);
	    Element printDebug = (Element)gameSetting.getElementsByTagName("print_debug").item(0);
	    Element updateThreshold = (Element)gameSetting.getElementsByTagName("update_threshold").item(0);
	    
	    GameSetting.NUM_GOODS = Integer.parseInt(numGoods.getFirstChild().getNodeValue());
	    GameSetting.NUM_AGENT = Integer.parseInt(numAgents.getFirstChild().getNodeValue());
	    GameSetting.NUM_ITERATION = Integer.parseInt(numIterations.getFirstChild().getNodeValue());
	    GameSetting.NUM_SIMULATION = Integer.parseInt(numSimulations.getFirstChild().getNodeValue());
	    GameSetting.VALUE_UPPER_BOUND = Integer.parseInt(valueUpperBound.getFirstChild().getNodeValue());
	    GameSetting.PRINT_DEBUG = Boolean.parseBoolean(printDebug.getFirstChild().getNodeValue());
	    GameSetting.UPDATE_THRESHOLD = Double.parseDouble(updateThreshold.getFirstChild().getNodeValue());
	    
	    game = gameType.getFirstChild().getNodeValue();
	    
	    NodeList agents = root.getElementsByTagName("agent");
	    
	    if (!mode.equalsIgnoreCase("training") && agents.getLength() != GameSetting.NUM_AGENT)
	    {
	    	System.out.println("Incomplete XML file: Number of agents mismatch");
	    	System.exit(-1);
	    }
	    
	    for (int i=0;i<agents.getLength();i++)
	    {
	    	Element agent = (Element)agents.item(i);
	    	int index = Integer.parseInt(agent.getAttribute("index"));
	    	String strategyName = "org.srg.scpp_im.strategy." + agent.getFirstChild().getNodeValue();
	    	System.out.println(strategyName);
	    	try
	    	{
		    	Class agentClass = Class.forName(strategyName);
		        Constructor con = agentClass.getConstructor(int.class);
		        Strategy s = (Strategy)con.newInstance(index);
		        strategies.add(s);
	    	}
	    	catch (Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    }
	}*/
}
