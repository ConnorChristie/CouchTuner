package me.connor.couchtuner;

public class Video
{
	private String title;
	private String link;

	public Video(String title, String link)
	{
		this.title = title;
		this.link = link;
	}

	public String getTitle()
	{
		return title;
	}

	public String getLink()
	{
		return link;
	}

	public String toString()
	{
		return title;
	}
}
