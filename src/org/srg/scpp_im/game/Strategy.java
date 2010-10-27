package org.srg.scpp_im.game;

import java.util.BitSet;
import java.util.Map;

public interface Strategy {
	public int[] bid(InformationState state);
	public int getIndex();
	public Map<BitSet, Integer> getTypeDist();
	public void setTypeDist(Map<BitSet, Integer> typeDist);
	public <T>void setPricePrediction(T pp);
	public int getCurrentSurplus(InformationState s);
	public void addObservation(InformationState s);
	public void setNewPrediction();
	public void printPrediction();
	public double getMaxDist();
	public boolean isSingleUnitDemand();
}
