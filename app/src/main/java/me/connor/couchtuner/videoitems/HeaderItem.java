package me.connor.couchtuner.videoitems;

public class HeaderItem implements Item
{
	private char header;

	public HeaderItem(char header)
	{
		this.header = header;
	}

	public char getHeader()
	{
		return header;
	}

	@Override
	public boolean isHeader()
	{
		return true;
	}
}
