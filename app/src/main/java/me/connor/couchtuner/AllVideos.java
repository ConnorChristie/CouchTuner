package me.connor.couchtuner;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

import me.connor.couchtuner.videolibrary.PinnedSectionListView;

public class AllVideos extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_all_videos);

		((ListView) findViewById(R.id.allVideos)).setAdapter(new MyPinnedSectionListAdapter());
	}

	private class MyPinnedSectionListAdapter extends BaseAdapter implements PinnedSectionListView.PinnedSectionListAdapter
	{
		//Comment
		@Override
		public boolean isItemViewTypePinned(int viewType)
		{
			return true;
		}

		@Override
		public int getCount()
		{
			return 1;
		}

		@Override
		public Object getItem(int position)
		{
			return "Connor";
		}

		@Override
		public long getItemId(int position)
		{
			return R.layout.video_cell;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.video_cell, null);
			}

			return convertView;
		}
	}
}
