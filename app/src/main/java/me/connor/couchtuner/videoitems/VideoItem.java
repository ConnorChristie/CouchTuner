package me.connor.couchtuner.videoitems;

public class VideoItem implements Item, Comparable<VideoItem>
{
	private String title;
	private String link;

	public VideoItem(String title, String link)
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

	@Override
	public boolean isHeader()
	{
		return false;
	}

	@Override
	public int compareTo(VideoItem another)
	{
		return getTitle().compareTo(another.getTitle());
	}

	public String toString()
	{
		return title;
	}
}
