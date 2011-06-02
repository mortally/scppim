package org.srg.scpp_im.analysis;

public class StatAnalysisRunner
{
	public static void main(String[] args)
	{
		StatAnalysis sa = new StatAnalysis();
		//sa.TwoSampleTtest(args[0], args[1]);
		sa.priceDistAnalyze(args[0], Integer.parseInt(args[1]));
	}
}
