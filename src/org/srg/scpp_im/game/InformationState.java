package org.srg.scpp_im.game;

/**
 * The class contains information about the current status of an auction game
 */
public class InformationState extends GameSetting {
	
	/** The number of goods. */
	private int numGoods;
	
	/** The current round. */
	private int round;
	
	/** The current bid price. */
	private double[] bidPrice;
	
	/** The current top price. */
	private double[] topPrice;
	
	/** The current top average price. */
	private double[] topAvgPrice;
	
	/** The bids. */
	private double[][] bids;
	
	/** The indices of winning agents for each good. */
	private int[] bidWinning;
	
	/**
	 * Instantiates a new information state.
	 *
	 * @param numGoods the number of goods
	 */
	public InformationState(int numGoods)
	{
		this.numGoods = numGoods;
		bids = new double[NUM_GOODS][HIERARCHICAL_REDUCTION_LEVEL * NUM_AGENT];
		bidPrice = new double[numGoods];
		bidWinning = new int[numGoods];
	}
	
	/**
	 * Gets the current bid price.
	 *
	 * @return the current bid price for each good
	 */
	public double[] getCurrentBidPrice()
	{
		return bidPrice;
	}
	
	/**
	 * Gets the indices of agents winning each of the goods currently
	 *
	 * @return the indices of winning agents
	 */
	public int[] getCurrentBidWinning()
	{
		return bidWinning;
	}
	
	/**
	 * Gets the current top price for each good.
	 *
	 * @return the current top price
	 */
	public double[] getCurrentTopPrice()
	{
		return topPrice;
	}
	
	/**
	 * Gets the current top average price.
	 *
	 * @return the current top average price
	 */
	public double[] getCurrentTopAvgPrice()
	{
		return topAvgPrice;
	}
	
	/**
	 * Sets the current bid price.
	 *
	 * @param newBidPrice the new bid price
	 */
	public void setBidPrice(double[] newBidPrice)
	{
		if (newBidPrice.length != numGoods) return;
		bidPrice = newBidPrice;
	}
	
	/**
	 * Sets the top price.
	 *
	 * @param newTopPrice the new top price
	 */
	public void setTopPrice(double[] newTopPrice)
	{
		topPrice = newTopPrice;
	}
	
	/**
	 * Sets the top average price.
	 *
	 * @param newTopAvgPrice the new top average price
	 */
	public void setTopAvgPrice(double[] newTopAvgPrice)
	{
		topAvgPrice = newTopAvgPrice;
	}
	
	/**
	 * Sets the bid winning.
	 *
	 * @param newBidWinning the new bid winning
	 */
	public void setBidWinning(int[] newBidWinning)
	{
		if (newBidWinning.length != numGoods) return;
		bidWinning = newBidWinning;
	}
	
	/**
	 * Sets the bids.
	 *
	 * @param newBids the new bids
	 */
	public void setBids(double[][] newBids)
	{
		bids = newBids;
	}
	
	/**
	 * Sets the round.
	 *
	 * @param r the new round
	 */
	public void setRound(int r)
	{
		round = r;
	}
	
	/**
	 * Gets the current round.
	 *
	 * @return the current round
	 */
	public int getRound()
	{
		return round;
	}
}
