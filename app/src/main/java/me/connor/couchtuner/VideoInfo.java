package me.connor.couchtuner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.connor.couchtuner.videolibrary.VideoEnabledWebChromeClient;
import me.connor.couchtuner.videolibrary.VideoEnabledWebView;

public class VideoInfo extends Activity
{
	private VideoEnabledWebView webView;
	private VideoEnabledWebChromeClient webChromeClient;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video_info);

		Intent callingIntent = getIntent();

		String videoTitle = callingIntent.getExtras().getString("videoTitle");
		Bitmap videoImage = (Bitmap) callingIntent.getExtras().get("videoImage");
		String videoLink = callingIntent.getExtras().getString("videoLink");

		((TextView) findViewById(R.id.videoTitle)).setText(videoTitle);
		((TextView) findViewById(R.id.videoInfo)).setMovementMethod(new ScrollingMovementMethod());
		((ImageView) findViewById(R.id.videoImage)).setImageBitmap(videoImage);

		new LoadInfoTask().execute(videoLink);

		findViewById(R.id.infoBack).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				finish();
			}
		});
	}

	private class LoadInfoTask extends AsyncTask<String, Void, String[]>
	{
		protected String[] doInBackground(String... params)
		{
			try
			{
				Document couchDoc = Jsoup.connect(params[0]).get();
				Elements post = couchDoc.select(".post");

				Element desc = post.select(".entry > p").first();
				Element posted = post.select(".descr").first();
				Element watch = post.select(".entry a").first();

				return new String[] { desc.text(), posted.text(), watch.attr("href") };
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return new String[0];
		}

		protected void onPostExecute(String[] result)
		{
			//findViewById(R.id.infoProgress).setVisibility(View.INVISIBLE);

			((TextView) findViewById(R.id.videoInfo)).setText(result[0]);
			((TextView) findViewById(R.id.videoPosted)).setText(result[1]);

			new LoadVideosTask().execute(result[2]);
		}
	}

	private class LoadVideosTask extends AsyncTask<String, Void, String[]>
	{
		protected String[] doInBackground(String... params)
		{
			try
			{
				Document couchDoc = Jsoup.connect(params[0]).get();
				Elements videos = couchDoc.select(".postTabs_divs");

				List<String> videoUrls = new ArrayList<>();

				for (Element e : videos.toArray(new Element[videos.size()]))
				{
					videoUrls.add(e.select("iframe").first().attr("src"));
				}

				return videoUrls.toArray(new String[videoUrls.size()]);
			} catch (IOException e)
			{
				e.printStackTrace();
			}

			return new String[0];
		}

		protected void onPostExecute(String[] result)
		{
			findViewById(R.id.infoProgress).setVisibility(View.INVISIBLE);
			findViewById(R.id.webVideo).setVisibility(View.VISIBLE);

			webView = (VideoEnabledWebView) findViewById(R.id.webVideo);

			webChromeClient = new VideoEnabledWebChromeClient(findViewById(R.id.videoLayout), (ViewGroup) findViewById(R.id.videoLayout), findViewById(R.id.videoLayout), webView)
			{
				@Override
				public void onProgressChanged(WebView view, int progress)
				{
					// Your code...
				}
			};

			webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
			{
				@Override
				public void toggledFullscreen(boolean fullscreen)
				{
					// Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
					if (fullscreen)
					{
						WindowManager.LayoutParams attrs = getWindow().getAttributes();
						attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
						attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						getWindow().setAttributes(attrs);
						if (android.os.Build.VERSION.SDK_INT >= 14)
						{
							//noinspection all
							getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
						}
					} else
					{
						WindowManager.LayoutParams attrs = getWindow().getAttributes();
						attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
						attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
						getWindow().setAttributes(attrs);
						if (android.os.Build.VERSION.SDK_INT >= 14)
						{
							//noinspection all
							getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
						}
					}

				}
			});
			webView.setWebChromeClient(webChromeClient);

			webView.getSettings().setJavaScriptEnabled(true);
			webView.loadUrl(result[1]);
		}
	}
}