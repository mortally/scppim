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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.yaml.snakeyaml.*;

public class SelfContainedGameRunner extends GameSetting {
	
	private static Document dom;
	private static String game;
	private static ArrayList<Strategy> strategies;
	private static double[][] avgPrices;
	private static double[] avgPrice ;
	private static BitSet[] bitVector;
	
	public static void main(String[] args)
	{
			
		String fileName = args[0];
		strategies = new ArrayList<Strategy>();
		parseSimulationSpecYAML(fileName);
		//parseGameSettingXML(fileName);
		
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
	        Constructor con = serverClass.getConstructor(new Class[]{});
	        
	        Register r = (Register)con.newInstance();
	        
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
	
	private static void parseSimulationSpecYAML(String file)
	{
		try
		{
			InputStream input = new FileInputStream(new File(file));
			Yaml yaml = new Yaml();
			
			Iterator<Object> spec = yaml.loadAll(input).iterator();
			List<String> strNames = (List<String>)spec.next();
			LinkedHashMap config = (LinkedHashMap)spec.next();
			
			GameSetting.NUM_AGENT = Integer.parseInt(config.get("num_agents").toString());
			
			if (GameSetting.NUM_AGENT != strNames.size())
			{
				System.out.println("Invalid YAML file: Number of agents mismatch");
		    	System.exit(-1);
			}
			
			GameSetting.NUM_GOODS = Integer.parseInt(config.get("num_goods").toString());
			assert GameSetting.NUM_GOODS > 0;
			GameSetting.NUM_ITERATION = Integer.parseInt(config.get("num_iterations").toString());
			assert GameSetting.NUM_ITERATION > 0;
			GameSetting.NUM_SIMULATION = Integer.parseInt(config.get("num_simulations").toString());
			assert GameSetting.NUM_SIMULATION > 0;
			GameSetting.PRINT_DEBUG = Boolean.parseBoolean(config.get("print_debug").toString());
			GameSetting.UPDATE_THRESHOLD = Double.parseDouble(config.get("update_threshold").toString());
			GameSetting.VALUE_UPPER_BOUND = Integer.parseInt(config.get("value_upper_bound").toString());
			game = config.get("game_type").toString();
			
			for (int i=0;i<strNames.size();i++)
		    {
		    	String strategyName = "org.srg.scpp_im.strategy." + strNames.get(i);
		    	System.out.println(strategyName);

		    	Class agentClass = Class.forName(strategyName);
		    	Constructor con = agentClass.getConstructor(int.class);
		        Strategy s = (Strategy)con.newInstance(i+1);
		        strategies.add(s);
		    }
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private static void parseGameSettingXML(String file)
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
	    
	    if (agents.getLength() != GameSetting.NUM_AGENT)
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
	}
}
