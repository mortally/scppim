package org.srg.scpp_im.strategy;

public class OSSCDP_BidEvaluatorSMU_E2S32K16 extends
		OSSCDP_BidEvaluatorSMU {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_BidEvaluatorSMU_E2S32K16(int index)
	{
		super(index);
		this.NUM_SAMPLE = 2;
		this.NUM_SCENARIO = 32;
		this.NUM_CANDIDATE_BID = 16;
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
}
