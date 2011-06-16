package org.srg.scpp_im.strategy;

public class OSSCDP_BidEvaluatorSMU_E4S32K8 extends
		OSSCDP_BidEvaluatorSMU {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_BidEvaluatorSMU_E4S32K8(int index)
	{
		super(index);
		this.NUM_SAMPLE = 4;
		this.NUM_SCENARIO = 32;
		this.NUM_CANDIDATE_BID = 8;
	}

	// Override
	public String getPPName()
	{
		return "OSSCDP_StraightMU";
	}
}
