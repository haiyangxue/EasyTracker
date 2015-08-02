package com.pratra.easyshare.ui.horizontallistview;

import com.pratra.easyshare.R;
import java.util.ArrayList;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

public class HorizontalListViewAdapter extends ArrayAdapter<Photo> {
	private ArrayList<Photo> mIconIDs;
	private Context mContext;
	private LayoutInflater mInflater;
	Bitmap iconBitmap;
	private int selectIndex = -1;

	public HorizontalListViewAdapter(Context context, ArrayList<Photo> items) {
		super(context, 0, items);
		this.mContext = context;
		this.mIconIDs = items;
		mInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);// LayoutInflater.from(mContext);
	}

	@Override
	public int getCount() {
		return mIconIDs.size();
	}

	@Override
	public Photo getItem(int position) {
		return mIconIDs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Check if an existing view is being reused, otherwise inflate the view
		ViewHolder holder;// / view lookup cache stored in tag

		if (convertView == null) {
			holder = new ViewHolder();
			convertView = mInflater
					.inflate(R.layout.horizontal_list_item, null);
			holder.mImage = (ImageView) convertView
					.findViewById(R.id.img_list_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		if (position == selectIndex) {
			convertView.setSelected(true);
		} else {
			convertView.setSelected(false);
		}

		// iconBitmap = getPropThumnail(mIconIDs.get(position));
		// holder.mImage.setImageBitmap(iconBitmap);
		holder.mImage.setImageBitmap(mIconIDs.get(position).getBitmap());

		return convertView;
	}

	private static class ViewHolder {
		private ImageView mImage;
	}

	private Bitmap getPropThumnail(Photo photo) {
		Bitmap b = photo.getBitmap();
		int w = mContext.getResources().getDimensionPixelOffset(
				R.dimen.thumnail_default_width);
		int h = mContext.getResources().getDimensionPixelSize(
				R.dimen.thumnail_default_height);

		Bitmap thumBitmap = ThumbnailUtils.extractThumbnail(b, w, h);

		return thumBitmap;
	}

	public void setSelectIndex(int i) {
		selectIndex = i;
	}
}