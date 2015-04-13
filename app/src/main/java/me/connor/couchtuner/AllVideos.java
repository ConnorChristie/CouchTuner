package me.connor.couchtuner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import me.connor.couchtuner.listadapter.VideosAdapter;
import me.connor.couchtuner.videoitems.HeaderItem;
import me.connor.couchtuner.videoitems.Item;
import me.connor.couchtuner.videoitems.VideoItem;

public class AllVideos extends Activity
{
	private VideosAdapter adapter;
	private ListView listView;

	private List<VideoItem> items = new ArrayList<>();
	private List<Item> itemsSections = new ArrayList<>();

	private Filter filter = new ArrayFilter();

	private final Object mLock = new Object();

	@Override
	public void onCreate(Bundle args)
	{
		super.onCreate(args);
		setContentView(R.layout.activity_all_videos);

		listView = (ListView) findViewById(R.id.allVideos);

		new LoadVideosTask().execute();

		listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long duration)
			{
				VideoItem item = (VideoItem) adapter.getItem(position);
				Intent videoInfoIntent = new Intent(AllVideos.this, EpisodeList.class);

				videoInfoIntent.putExtra("videoTitle", item.getTitle());
				videoInfoIntent.putExtra("videoLink", item.getLink());

				startActivity(videoInfoIntent);
			}
		});

		EditText searchText = (EditText) findViewById(R.id.searchVideos);

		searchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
			{
				if (actionId == EditorInfo.IME_ACTION_SEARCH)
				{
					// hide virtual keyboard
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);

					listView.requestFocus();

					return true;
				}

				return false;
			}
		});

		searchText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{
				filter.filter(s);
			}

			@Override
			public void afterTextChanged(Editable s)
			{

			}
		});
	}

	protected class LoadVideosTask extends AsyncTask<Void, Void, SortedMap<String, SortedMap<String, String>>>
	{
		public LoadVideosTask()
		{
		}

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
							SortedMap<String, String> list = new TreeMap<>();

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
				SortedMap<String, String> videos = result.get(section);

				for (String title : videos.keySet())
				{
					items.add(new VideoItem(title, videos.get(title)));
				}
			}

			setVideosAdapter(items);
		}
	}

	private void setVideosAdapter(List<VideoItem> items)
	{
		itemsSections.clear();

		char prevChar = ' ';

		for (VideoItem vi : items)
		{
			char firstChar = vi.getTitle().toCharArray()[0];

			if (Character.isDigit(firstChar)) firstChar = '#';

			if (firstChar != prevChar)
			{
				itemsSections.add(new HeaderItem(firstChar));

				prevChar = firstChar;
			}

			itemsSections.add(vi);
		}

		if (adapter == null)
		{
			adapter = new VideosAdapter(this, R.layout.list_item, itemsSections);

			listView.setAdapter(adapter);
		} else
		{
			adapter.notifyDataSetChanged();
		}
	}

	private class ArrayFilter extends Filter
	{
		@Override
		protected FilterResults performFiltering(CharSequence prefix)
		{
			FilterResults results = new FilterResults();

			if (prefix == null || prefix.length() == 0)
			{
				ArrayList<VideoItem> list;

				synchronized (mLock)
				{
					list = new ArrayList<>(items);
				}

				results.values = list;
				results.count = list.size();
			} else
			{
				String prefixString = prefix.toString().toLowerCase();

				ArrayList<VideoItem> values;

				synchronized (mLock)
				{
					values = new ArrayList<>(items);
				}

				final int count = values.size();
				final ArrayList<VideoItem> newValues = new ArrayList<>();

				for (int i = 0; i < count; i++)
				{
					final VideoItem value = values.get(i);
					final String valueText = value.toString().toLowerCase();

					// First match against the whole, non-splitted value
					if (valueText.startsWith(prefixString))
					{
						newValues.add(value);
					} else
					{
						final String[] words = valueText.split(" ");
						final int wordCount = words.length;

						// Start at index 0, in case valueText starts with space(s)
						for (int k = 0; k < wordCount; k++)
						{
							if (words[k].startsWith(prefixString))
							{
								newValues.add(value);

								break;
							}
						}
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint, FilterResults results)
		{
			setVideosAdapter((List<VideoItem>) results.values);
		}
	}
}
