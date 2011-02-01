package org.srg.scpp_im.game;

// temporary interface to provide parameter values

public class GameSetting {
	public static int NUM_GOODS = 3;
	public static int NUM_AGENT = 3;
	public static int NUM_ITERATION = 1;
	public static int NUM_SIMULATION = 1;
	public static int NUM_SAMPLE = 200;
	public static int NUM_SCENARIO = 100;
	public static int NUM_CANDIDATE_BID = 1;
	public static int VALUE_UPPER_BOUND = 127;
	public static int HIERARCHICAL_REDUCTION_LEVEL = 2;
	public static boolean PRINT_DEBUG = true;
	public static boolean PRINT_OUTPUT = false;
	public static double UPDATE_THRESHOLD = 0.5;
	public static double MIN_POINT_DIST_TO_TERMINATE = 0.001;
	public static double MIN_DISTRIBUTION_DIST_TO_TERMINATE = 0.01;
	public static String GAME_TYPE = "";
	public static String DIST_TYPE = "";
	public static String SIMUL_PATH = "";
	
	public static final int TRAINING_MODE = 1;
	public static final int PRODUCTION_MODE = 2;
	
	public static final int POINT = 1;
	public static final int DISTRIBUTION = 2;
}
