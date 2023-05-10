/*
 * 	NAME: 			EMILIO CRUZ
 * 	APPLICATION:	EYE COLOR CHANGER
 * 	ASSIGNMENT: 	FINAL PROJECT (SER305)
 * 	DATE: 			MAY 2023 
 */

import org.opencv.core.Point;

// holds bounds and center point for a detected eye
public class EyeSpecs 
{
	Point eyeCenter;
	int leftBound, rightBound, topBound, botBound;
	
	public EyeSpecs(Point center, int left, int right, int top, int bot)
	{
		eyeCenter = center;
		leftBound = left;
		rightBound = right;
		topBound = top;
		botBound = bot;
	}
	
	// format toString() for EyeSpecs
	public String toString()
	{
		String string = "Center: " + eyeCenter + ", Left: " + leftBound + ", Right: " + rightBound +
				", Top: " + topBound + ", Bottom: " + botBound;
		
		return string;
	}
}
