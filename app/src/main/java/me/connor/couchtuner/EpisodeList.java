package me.connor.couchtuner;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Map;

import me.connor.couchtuner.episodeitems.EpisodeItem;


public class EpisodeList extends ActionBarActivity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_episode_list);

		Intent callingIntent = getIntent();

		setTitle(callingIntent.getExtras().getString("videoTitle"));
	}

	private class LoadEpisodesTask extends AsyncTask<String, Void, Map<String, EpisodeItem>>
	{
		@Override
		protected Map<String, EpisodeItem> doInBackground(String... params)
		{
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.menu_episode_list, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		int id = item.getItemId();

		if (id == R.id.action_info)
		{
			Log.d("Info", "Show info");

			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
