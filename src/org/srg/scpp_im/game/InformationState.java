package org.srg.scpp_im.game;

public class InformationState {
	
	private int numGoods;
	private int[] bidPrice;
	private int[] bidWinning;
	
	public InformationState(int numGoods)
	{
		this.numGoods = numGoods;
		bidPrice = new int[numGoods];
		bidWinning = new int[numGoods];
	}
	
	public int[] getCurrentBidPrice()
	{
		return bidPrice;
	}
	
	public int[] getCurrentBidWinning()
	{
		return bidWinning;
	}
	
	public void setBidPrice(int[] newBidPrice)
	{
		if (newBidPrice.length != numGoods) return;
		bidPrice = newBidPrice;
	}
	
	public void setBidWinning(int[] newBidWinning)
	{
		if (newBidWinning.length != numGoods) return;
		bidWinning = newBidWinning;
	}
}
