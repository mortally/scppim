package org.srg.scpp_im.game;

// temporary interface to provide constant parameter values

/**
 * GameSetting class provides required parameters for SPSB games.
 * @author Dong Youn Yoon
 */
public class GameSetting {
	
	/** The number of goods in the market. */
	public static int NUM_GOODS = 5;
	
	/** The number of players. */
	public static int NUM_PLAYER = 5;

	/** The number of price distributions that mix strategies use.
	 * 	This parameter has been only used for experimental purposes. */
	public static int NUM_DIST_MIX = 5;
	
	/** The maximum number of iterations for price vector training */
	public static int NUM_ITERATION = 1;
	
	/** The number of simulations in a single iteration. */
	public static int NUM_SIMULATION = 1;
	
	/** The number of samples that distribution strategies generate
	 *  in order to create a bid. */
	public static int NUM_SAMPLE = 200;
	
	/** The number of market scenarios for evaluating 
	 * 	the goodness of a bid */
	public static int NUM_SCENARIO = 100;
	
	/** The number of candidate bids for BidEvaluators. */
	public static int NUM_CANDIDATE_BID = 1;
	
	/** The upper bound for maximum valuation of a good. */
	public static int VALUE_UPPER_BOUND = 50;
	
	/** The number of players reduced by hierarchical game reduction.
	 * 	4 players and reduction level of 2 imply that there are 8 agents,
	 *  where each player controls two of the agents with same strategy */
	public static int HIERARCHICAL_REDUCTION_LEVEL = 2;
	
	/** The total number of agents, equivalent to NUM_AGENT. */
	public static int TOTAL_AGENTS = NUM_PLAYER * HIERARCHICAL_REDUCTION_LEVEL;
	
	/** The total number of agents. */
	public static int NUM_AGENT = NUM_PLAYER * HIERARCHICAL_REDUCTION_LEVEL;
	
	/** The flag for printing debugging output. */
	public static boolean PRINT_DEBUG = false;
	
	/** The flag for printing general output. */
	public static boolean PRINT_OUTPUT = false;
	
	/** The flag to enable/disable an analyzer that calculates
	 * 	cumulative statistics. */
	public static boolean ENABLE_ANALYZER = false;
	
	/** The flag to specify whether we train a price vector for
	 * 	every possible profile. */
	public static boolean PROFILE_BASED = false;
	
	/** The flag to record the highest bid rather than
	 * 	second price. */
	public static boolean HIGHEST_BID_PREDICTION = false;
	
	/** The flag for random initial predictions in order
	 *  to test . */
	public static boolean RANDOM_INITIAL_PREDICTION = false;
	
	/** The flag for getting efficiency of the game result. 
	 *  Setting this flag to true will incur a significant computation
	 *  cost to calculate the optimal value for every agent.*/
	public static boolean GET_EFFICIENCY = false;
	
	/** The probability of each price vector being updated 
	 *  during the training. */
	public static double UPDATE_THRESHOLD = 0.5;
	
	/** The threshold to terminate the training of price vector
	 *  for point price predictions. */
	public static double MIN_POINT_DIST_TO_TERMINATE = 0.001;
	
	/** The threshold to terminate the training of price vector
	 *  for distribution price predictions. */
	public static double MIN_DISTRIBUTION_DIST_TO_TERMINATE = 0.01;
	
	/** The game type. */
	public static String GAME_TYPE = "";
	
	/** The type of user valuation distribution. */
	public static String DIST_TYPE = "";
	
	/** The location of simulation_spec.yaml. */
	public static String SIMUL_PATH = "";
	
	/** The Constant TRAINING_MODE. */
	public static final int TRAINING_MODE = 1;
	
	/** The Constant PRODUCTION_MODE. */
	public static final int PRODUCTION_MODE = 2;
	
	/** The Constant POINT. */
	public static final int POINT = 1;
	
	/** The Constant DISTRIBUTION. */
	public static final int DISTRIBUTION = 2;
	
	/** The Constant DISTRIBUTION_MIX. */
	public static final int DISTRIBUTION_MIX = 3;
}
