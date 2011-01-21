package org.srg.scpp_im.game;

import java.util.BitSet;
import java.util.Map;

public interface Strategy {
	
	public int[] bid(InformationState state);
	public int getIndex();
	public String getName();
	public Map<BitSet, Integer> getTypeDist();
	public void setTypeDist(Map<BitSet, Integer> typeDist);
	public <T>void setPricePrediction(T pp);
	public <T>T getPricePrediction();
	public int getCurrentSurplus(InformationState s);
	public int getPredictionType();
	public void addObservation(InformationState s);
	public void setNewPrediction();
	public void setNewPredictionAverage();
	public void resetObservation();
	public void printPrediction();
	public double getMaxDist();
	public double getAverageUtility();
	public boolean isSingleUnitDemand();
}
