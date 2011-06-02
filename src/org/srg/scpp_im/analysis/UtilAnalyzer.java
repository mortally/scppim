package org.srg.scpp_im.analysis;

import org.srg.scpp_im.game.*;

public class UtilAnalyzer extends GameSetting
{
	public static double bestCandidateUtil = 0.0;
	
	public static void addBestCandidateUtil(double util)
	{
		bestCandidateUtil += util;
	}
}