package org.srg.scpp_im.game;

import java.util.BitSet;
import java.util.Map;

public interface Strategy {
	
	public double[] bid(InformationState state);
	public int getIndex();
	public String getName();
	public String getPPName();
	public Map<BitSet, Integer> getTypeDist();
	public void setTypeDist(Map<BitSet, Integer> typeDist);
	public void setTypeDist(Map<BitSet, Integer> typeDist, int length, int[] deadlineValues);
	public <T>void setPricePrediction(T pp);
	public <T>T getPricePrediction();
	public double getCurrentSurplus(InformationState s);
	public double[] getUtilityRecord();
	public int getPredictionType();
	public void addObservation(InformationState s);
	public void setNewPrediction();
	public void setNewPredictionAverage(int currentIt);
	public void resetObservation();
	public void printPrediction();
	public double getMaxDist();
	public double getMaxDist(double[][] pp);
	public double getAverageUtility();
	public double getAverageValue();
	public boolean isSingleUnitDemand();
}
