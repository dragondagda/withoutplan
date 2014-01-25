package de.ggj14bremen.withoutplan.model;



public class Figure
{
	public enum Orientation
	{
		TOP,
		BOTTOM,
		LEFT,
		RIGHT;
		
		public final float getAngle()
		{
			switch (this)
			{
				case TOP: 		return 0f;
				case BOTTOM: 	return .5f;
				case LEFT: 		return .75f;
				case RIGHT: 	return .25f;
				default:		return 0f;
			}
		}
	}
	
	private int x;
	private int y;
	private Orientation orientation;
	private WPColor color;
	
	public Figure()
	{
		this.x = -1;
		this.y = -1;
	}
	
	public boolean hasValidPosition()
	{
		return x >= 0;
	}
	
	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Orientation getOrientation()
	{
		return orientation;
	}
	
	public void setOrientation(Orientation orientation)
	{
		this.orientation = orientation;
	}
	
	public WPColor getColor()
	{
		return color;
	}
	
	public void setColor(WPColor color)
	{
		this.color = color;
	}
	
	public void setPosition(int x, int y)
	{
		this.x = x;
		this.y = y;
	}
	
	
}
