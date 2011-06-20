package org.srg.scpp_im.game;

import java.util.BitSet;
import java.util.Map;

/**
 * The Interface Strategy that every bidding strategy has to implement in
 * order to participate in an auction game.
 */
public interface Strategy {
	
	/**
	 * Given the state of an auction, an agent places a bid.
	 *
	 * @param state the state of an auction game
	 * @return the bid placed
	 */
	public double[] bid(InformationState state);
	
	/**
	 * Gets the index of an agent.
	 *
	 * @return the index
	 */
	public int getIndex();
	
	/**
	 * Gets the name of an agent.
	 *
	 * @return the name
	 */
	public String getName();
	
	/**
	 * Gets the name of price prediction an agent uses.
	 *
	 * @return the pP name
	 */
	public String getPPName();
	
	/**
	 * Gets the type/value distribution.
	 *
	 * @return the type/value distribution
	 */
	public Map<BitSet, Integer> getTypeDist();
	
	/**
	 * Sets the type/value distribution.
	 *
	 * @param typeDist the type/value distribution
	 */
	public void setTypeDist(Map<BitSet, Integer> typeDist);
	
	/**
	 * Sets the type/value distribution specific to market-based scheduling.
	 *
	 * @param typeDist the type/value distribution
	 * @param length the required job length
	 * @param deadlineValues the deadline values
	 */
	public void setTypeDist(Map<BitSet, Integer> typeDist, int length, int[] deadlineValues);
	
	/**
	 * Sets the price prediction.
	 *
	 * @param <T> the generic type for price prediction
	 * @param pp the new price prediction
	 */
	public <T>void setPricePrediction(T pp);
	
	/**
	 * Gets the price prediction.
	 *
	 * @param <T> the generic type for price prediction
	 * @return the price prediction
	 */
	public <T>T getPricePrediction();
	
	/**
	 * Gets the current surplus/utility of an agent.
	 *
	 * @param s the state of an auction game
	 * @return the current surplus/utility of an agent
	 */
	public double getCurrentSurplus(InformationState s);
	
	/**
	 * Gets the utility record for all samples.
	 *
	 * @return the utility record
	 */
	public double[] getUtilityRecord();
	
	/**
	 * Gets the prediction type (i.e. point/distribution).
	 *
	 * @return the prediction type
	 */
	public int getPredictionType();
	
	/**
	 * Adds the price observation.
	 *
	 * @param s the final state of an auction
	 */
	public void addObservation(InformationState s);
	
	/**
	 * Sets the new prediction based on observed price information.
	 */
	public void setNewPrediction();
	
	/**
	 * Sets the new prediction according to the current and observed
	 * price information.
	 *
	 * @param currentIt the count of the current iteration
	 */
	public void setNewPredictionAverage(int currentIt);
	
	/**
	 * Reset price observation.
	 */
	public void resetObservation();
	
	/**
	 * Prints the price prediction.
	 */
	public void printPrediction();
	
	/**
	 * Gets the maximum prediction distance between the previous and current
	 * price prediction.
	 *
	 * @return the maximum distance between prediction vectors
	 */
	public double getMaxDist();
	
	/**
	 * Gets the maximum prediction distance between the current and given
	 * price prediction vector.
	 *
	 * @param pp the price prediction vector
	 * @return the maximum distance between prediction vectors
	 */
	public double getMaxDist(double[][] pp);
	
	/**
	 * Gets the average utility.
	 *
	 * @return the average utility
	 */
	public double getAverageUtility();
	
	/**
	 * Gets the average acquired value.
	 * 
	 * @return the average value
	 */
	public double getAverageValue();
	
	/**
	 * Checks if an agent has a single-unit demand.
	 *
	 * @return true, if an agent has a single-unit demand
	 */
	public boolean isSingleUnitDemand();
}
