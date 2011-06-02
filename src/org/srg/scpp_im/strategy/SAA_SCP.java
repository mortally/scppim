package org.srg.scpp_im.strategy;

public class SAA_SCP extends SelfConfirmingPricePrediction
{
	private static final long serialVersionUID = 100L;
	
	public SAA_SCP(int index)
	{
		super(index);
		for (int i=0;i<NUM_GOODS;i++)
		{
			this.pricePrediction[i] = 0.0;
		}
	}

}
