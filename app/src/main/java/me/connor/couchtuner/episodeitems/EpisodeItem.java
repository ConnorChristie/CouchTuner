package me.connor.couchtuner.episodeitems;

public class EpisodeItem
{
	private String title;
	private String link;

	public EpisodeItem(String title, String link)
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
}
