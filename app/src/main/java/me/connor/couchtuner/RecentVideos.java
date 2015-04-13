package me.connor.couchtuner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class RecentVideos extends ActionBarActivity
{
	private int rowWidth = 4;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_recent_videos);

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		rowWidth = (int) Math.floor((metrics.widthPixels / metrics.density) / 90);

		new LoadRecentTask(this, (GridLayout) findViewById(R.id.videoGrid)).execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_recent_videos, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	private class LoadRecentTask extends AsyncTask<Void, Object[], Map<Integer, Object[]>>
	{
		private Context context;
		private GridLayout videoGrid;

		private int num = 1;
		private LinearLayout row;

		public LoadRecentTask(Context context, GridLayout videoGrid)
		{
			this.context = context;
			this.videoGrid = videoGrid;

			row = new LinearLayout(context);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			row.setGravity(Gravity.CENTER);
			row.setBaselineAligned(false);
			row.setPadding(0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, context.getResources().getDisplayMetrics()), 0);

			View v = new View(context);
			v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, context.getResources().getDisplayMetrics())));

			videoGrid.addView(v);
			videoGrid.addView(row);
		}

		protected Map<Integer, Object[]> doInBackground(Void... v)
		{
			Map<Integer, Object[]> videos = new HashMap<>();

			try
			{
				Document couchDoc = Jsoup.connect("http://www.couchtuner.eu/").get();
				Elements tvBoxes = couchDoc.select(".tvbox");

				Integer videoId = 101;

				for (Element e : tvBoxes.toArray(new Element[tvBoxes.size()]))
				{
					String imageUrl = "http://www.couchtuner.eu" + e.select("span").attr("style").replace("background-image: url(", "").replace(")", "");

					String prettyPrintedBodyFragment = Jsoup.clean(e.select("a").html(), "", Whitelist.none().addTags("br"),new Document.OutputSettings().prettyPrint(true));
					String videoTitle = Jsoup.clean(prettyPrintedBodyFragment, "", Whitelist.none(), new Document.OutputSettings().prettyPrint(false)).replace("0 comments", "").replace("&amp;", "&");

					videos.put(videoId, new Object[] { videoTitle, e.select("a").attr("href") });
					//videos.add(new Object[] {imageUrl, videoTitle.replace("0 comments", "")});

					publishProgress(new Object[] { videoId, imageUrl, videoTitle });

					videoId += 2;
				}
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return videos;
		}

		protected void onProgressUpdate(Object[]... values)
		{
			Integer videoId = (Integer) values[0][0];
			String image = (String) values[0][1];
			String videoTitle = (String) values[0][2];

			LinearLayout videoHolder = new LinearLayout(context);
			videoHolder.setId(videoId);
			videoHolder.setClickable(true);
			videoHolder.setOrientation(LinearLayout.VERTICAL);
			videoHolder.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
			videoHolder.setBaselineAligned(false);

			ImageView imageView = new ImageView(context);
			LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 75, context.getResources().getDisplayMetrics()));
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL;

			imageView.setId(videoId + 1);
			imageView.setLayoutParams(layoutParams);
			imageView.setImageResource(R.mipmap.no_image_available);
			new DownloadImageTask(imageView).execute(image);

			TextView title = new TextView(context);
			title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			title.setTextAppearance(context, R.style.TextAppearance_AppCompat_Small);
			title.setText(videoTitle.replace("Episode", "\nEpisode"));
			title.setGravity(Gravity.CENTER_HORIZONTAL);

			videoHolder.addView(imageView);
			videoHolder.addView(title);

			row.addView(videoHolder);

			if (num % rowWidth == 0)
			{
				row = new LinearLayout(context);
				row.setOrientation(LinearLayout.HORIZONTAL);
				row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
				row.setGravity(Gravity.CENTER_HORIZONTAL);
				row.setBaselineAligned(false);
				row.setPadding(0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, context.getResources().getDisplayMetrics()), 0);

				View v = new View(context);
				v.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics())));

				videoGrid.addView(v);
				videoGrid.addView(row);
			}

			num++;
		}

		protected void onPostExecute(Map<Integer, Object[]> result)
		{
			row = new LinearLayout(context);
			row.setOrientation(LinearLayout.HORIZONTAL);
			row.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
			row.setGravity(Gravity.CENTER_HORIZONTAL);
			row.setBaselineAligned(false);

			Button allVideos = new Button(context);
			allVideos.setText("All Videos");

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT
			);

			int five = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, context.getResources().getDisplayMetrics());

			params.setMargins(five, five, five, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics()));
			allVideos.setLayoutParams(params);

			row.addView(allVideos);
			videoGrid.addView(row);

			allVideos.setOnClickListener(new View.OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Intent videoInfoIntent = new Intent(RecentVideos.this, AllVideos.class);

					startActivity(videoInfoIntent);
				}
			});

			for (Integer i : result.keySet())
			{
				final Object[] info = result.get(i);

				LinearLayout videoHolder = (LinearLayout) findViewById(i);
				final ImageView videoImage = (ImageView) findViewById(i + 1);

				videoHolder.setOnClickListener(new View.OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						Intent videoInfoIntent = new Intent(RecentVideos.this, VideoInfo.class);

						videoInfoIntent.putExtra("videoTitle", (String) info[0]);
						videoInfoIntent.putExtra("videoImage", ((BitmapDrawable) videoImage.getDrawable()).getBitmap());
						videoInfoIntent.putExtra("videoLink", (String) info[1]);

						startActivity(videoInfoIntent);
					}
				});
			}
		}
	}

	protected class DownloadImageTask extends AsyncTask<String, Void, Bitmap>
	{
		private ImageView imageView;

		public DownloadImageTask(ImageView imageView)
		{
			this.imageView = imageView;
		}

		protected Bitmap doInBackground(String... url)
		{
			Bitmap icon = null;

			try
			{
				InputStream in = new java.net.URL(url[0]).openStream();
				icon = BitmapFactory.decodeStream(in);
			} catch (Exception e)
			{
				e.printStackTrace();

				icon = BitmapFactory.decodeResource(getResources(), R.mipmap.no_image_available);
			}

			return icon;
		}

		protected void onPostExecute(Bitmap result)
		{
			imageView.setImageBitmap(result);
		}
	}
}
