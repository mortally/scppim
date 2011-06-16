package org.srg.scpp_im.strategy;

public class OSSCDP_BidEvaluatorSMU_E1S32K32 extends
		OSSCDP_BidEvaluatorSMU {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_BidEvaluatorSMU_E1S32K32(int index)
	{
		super(index);
		this.NUM_SAMPLE = 1;
		this.NUM_SCENARIO = 32;
		this.NUM_CANDIDATE_BID = 32;
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
}
