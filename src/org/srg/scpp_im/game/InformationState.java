package org.srg.scpp_im.game;

public class InformationState extends GameSetting {
	
	private int numGoods;
	private int round;
	private double[] bidPrice;
	private double[] topPrice;
	private double[] topAvgPrice;
	private double[][] bids;
	private int[] bidWinning;
	
	public InformationState(int numGoods)
	{
		this.numGoods = numGoods;
		bids = new double[NUM_GOODS][HIERARCHICAL_REDUCTION_LEVEL * NUM_AGENT];
		bidPrice = new double[numGoods];
		bidWinning = new int[numGoods];
	}
	
	public double[] getCurrentBidPrice()
	{
		return bidPrice;
	}
	
	public int[] getCurrentBidWinning()
	{
		return bidWinning;
	}
	
	public double[] getCurrentTopPrice()
	{
		return topPrice;
	}
	
	public double[] getCurrentTopAvgPrice()
	{
		return topAvgPrice;
	}
	
	public void setBidPrice(double[] newBidPrice)
	{
		if (newBidPrice.length != numGoods) return;
		bidPrice = newBidPrice;
	}
	
	public void setTopPrice(double[] newTopPrice)
	{
		topPrice = newTopPrice;
	}
	
	public void setTopAvgPrice(double[] newTopAvgPrice)
	{
		topAvgPrice = newTopAvgPrice;
	}
	
	public void setBidWinning(int[] newBidWinning)
	{
		if (newBidWinning.length != numGoods) return;
		bidWinning = newBidWinning;
	}
	
	public void setBids(double[][] newBids)
	{
		bids = newBids;
	}
	
	public void setRound(int r)
	{
		round = r;
	}
	public int getRound()
	{
		return round;
	}
}
