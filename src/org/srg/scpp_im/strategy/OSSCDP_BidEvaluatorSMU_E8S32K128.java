package org.srg.scpp_im.strategy;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Random;

import org.srg.scpp_im.game.InformationState;

public class OSSCDP_BidEvaluatorSMU_E8S32K128 extends
		OSSCDP_BidEvaluatorSMU {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_BidEvaluatorSMU_E8S32K128(int index)
	{
		super(index);
		this.NUM_SAMPLE = 8;
		this.NUM_SCENARIO = 32;
		this.NUM_CANDIDATE_BID = 128;
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
	public double[] bid(InformationState s)
	{
		double[] newBid = sampleMV();
		return newBid;
	}
}
