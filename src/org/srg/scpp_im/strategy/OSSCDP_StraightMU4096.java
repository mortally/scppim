package org.srg.scpp_im.strategy;

public class OSSCDP_StraightMU4096 extends OSSCDP_StraightMU {
	
	private static final long serialVersionUID = 100L;
	
	public OSSCDP_StraightMU4096(int index)
	{
		super(index);
		this.NUM_SAMPLE = 4096;
	}
	
	public String getPPName()
	{
		return "OSSCDP_StraightMU64";
	}
}
