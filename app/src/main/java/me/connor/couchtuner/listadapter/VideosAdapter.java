package me.connor.couchtuner.listadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.connor.couchtuner.R;
import me.connor.couchtuner.videoitems.HeaderItem;
import me.connor.couchtuner.videoitems.Item;
import me.connor.couchtuner.videoitems.VideoItem;

public class VideosAdapter extends ArrayAdapter<Item>
{
	private Item objItem;

	private ViewHolderSectionName holderSection;
	private ViewHolderName holderName;

	private LayoutInflater vi;

	public VideosAdapter(Context context, int resource, List<Item> items)
	{
		super(context, resource, items);

		//this.items = items;
		this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent)
	{
		objItem = getItem(position);

		if (objItem.isHeader())
		{
			HeaderItem si = (HeaderItem) objItem;

			if (convertView == null || !convertView.getTag().equals(holderSection))
			{
				convertView = vi.inflate(R.layout.list_header, null);

				holderSection = new ViewHolderSectionName();
				convertView.setTag(holderSection);
			} else
			{
				holderSection = (ViewHolderSectionName) convertView.getTag();
			}

			holderSection.section = (TextView) convertView.findViewById(R.id.list_header_title);
			holderSection.section.setText(String.valueOf(si.getHeader()));

		} else
		{
			VideoItem ei = (VideoItem) objItem;

			if (convertView == null || !convertView.getTag().equals(holderName))
			{
				convertView = vi.inflate(R.layout.list_item, null);

				holderName = new ViewHolderName();
				convertView.setTag(holderName);
			} else
			{
				holderName = (ViewHolderName) convertView.getTag();
			}

			holderName.name = (TextView) convertView.findViewById(R.id.list_item_title);

			if (holderName.name != null)
			{
				holderName.name.setText(ei.getTitle());
			}
		}
		return convertView;
	}

	public static class ViewHolderName
	{
		public TextView name;
	}

	public static class ViewHolderSectionName
	{
		public TextView section;
	}
}
