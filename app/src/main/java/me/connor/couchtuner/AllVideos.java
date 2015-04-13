package me.connor.couchtuner;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import me.connor.couchtuner.listadapter.SeparatedListAdapter;

public class AllVideos extends Activity
{
	private AllVideos instance;

	// Adapter for ListView Contents
	private SeparatedListAdapter adapter;

	// ListView Contents
	private ListView listView;

	@Override
	public void onCreate(Bundle args)
	{
		super.onCreate(args);
		setContentView(R.layout.activity_all_videos);

		// Create the ListView Adapter
		adapter = new SeparatedListAdapter(instance = this);
		listView = (ListView) findViewById(R.id.allVideos);

		new LoadVideosTask().execute();

		// Listen for Click events
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration)
			{
				Video item = (Video) adapter.getItem(position);
				Toast.makeText(getApplicationContext(), item.getLink(), Toast.LENGTH_LONG).show();
			}
		});

		((EditText) findViewById(R.id.searchVideos)).addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				for (String section : instance.adapter.sections.keySet())
				{
					instance.adapter.sections.get(section).getFilter().filter(s);
				}

				instance.adapter.notifyDataSetChanged();
			}

			@Override
			public void afterTextChanged(Editable s)
			{

			}
		});
	}

	protected class LoadVideosTask extends AsyncTask<Void, Void, SortedMap<String, SortedMap<String, String>>>
	{
		public LoadVideosTask() { }

		protected SortedMap<String, SortedMap<String, String>> doInBackground(Void... args)
		{
			SortedMap<String, SortedMap<String, String>> videos = new TreeMap<>();

			try
			{
				Document couchDoc = Jsoup.connect("http://www.couchtuner.eu/tv-lists/").get();
				Elements columns = couchDoc.select(".entry > div");

				for (Element column : columns)
				{
					Element prevP = null;

					for (Element elem : column.children())
					{
						if (elem.nodeName().equals("p"))
						{
							prevP = elem;

							continue;
						} else if (elem.nodeName().equals("ul"))
						{
							SortedMap<String, String> list = new TreeMap<>(

							);

							for (Element li : elem.select("li"))
							{
								list.put(li.text(), li.select("a").attr("href"));
							}

							videos.put(prevP.text().isEmpty() ? "S" : (prevP.text().length() == 1 ? prevP.text() : prevP.text().substring(prevP.text().length() - 1, prevP.text().length())), list);
						}
					}
				}
			} catch (Exception e)
			{
				e.printStackTrace();
			}

			return videos;
		}

		protected void onPostExecute(SortedMap<String, SortedMap<String, String>> result)
		{
			for (String section : result.keySet())
			{
				List<Video> videoList = new ArrayList<>();
				SortedMap<String, String> videos = result.get(section);

				for (String title : videos.keySet())
				{
					videoList.add(new Video(title, videos.get(title)));
				}

				adapter.addSection(section, new ArrayAdapter<>(instance, R.layout.list_item, R.id.list_item_title, videoList));
			}

			listView.setAdapter(adapter);
		}
	}
}
