package com.pratra.easyshare.ui.fragment;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.pratra.easyshare.R;
import com.pratra.easyshare.httpapi.ApiHttpClient;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyImageCallback;
import com.pratra.easyshare.ui.horizontallistview.HorizontalListView;
import com.pratra.easyshare.ui.horizontallistview.HorizontalListViewAdapter;
import com.pratra.easyshare.ui.horizontallistview.Photo;
import com.pratra.easyshare.widget.CustomProgressDialog;

/**
 * 拍照Fragment的界面
 * 
 * @author
 */
public class PhotoFragment extends Fragment {

	public static String TAG = "TAG";
	// 自定义的照片墙
	HorizontalListView hListView;
	// 横向ListView的适配器
	HorizontalListViewAdapter hListViewAdapter;
	// ImageView组件用于显示照片，该照片是按下拍照按钮之后从服务器上接受到的远程主机所拍摄的照片
	ImageView previewImg;
	// 拍照按钮，向服务器请求拍照，服务器将控制指令转发给远程主机进行拍照
	ImageButton captureButton;
	// 待显示的图片数据
	ArrayList<Photo> items;

	View view;

	int count = 0;
	Photo photo;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_photo, container, false);
		
		initUI();

		return view;
	}

	public void initUI() {

		hListView = (HorizontalListView) view.findViewById(R.id.horizon_listview);
		previewImg = (ImageView) view.findViewById(R.id.image_preview);
		captureButton = (ImageButton) view.findViewById(R.id.capture_photo);

		captureButton.setOnClickListener(new CaptureButtonListener());

		// 待显示的图片数据
		items = new ArrayList<Photo>();
		hListViewAdapter = new HorizontalListViewAdapter(view.getContext(),
				items);
		hListView.setAdapter(hListViewAdapter);

		hListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				previewImg.setImageBitmap(items.get(position).getBitmap());
				hListViewAdapter.setSelectIndex(position);
				hListViewAdapter.notifyDataSetChanged();
			}
		});
	}

	private class CaptureButtonListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			final CustomProgressDialog progressDialog = new CustomProgressDialog(
					PhotoFragment.this.getActivity(), "正在加载中", R.anim.frame);
			progressDialog.show();
			progressDialog.setCanceledOnTouchOutside(false);
			
			ApiHttpClient.loadImage(PhotoFragment.this.getActivity()
					.getApplicationContext(), previewImg,
					R.drawable.image_background, R.drawable.image_background,
					new VolleyImageCallback() {
						@Override
						public void onSuccess(Bitmap bitmap, boolean isImmediate) {
							if (bitmap != null) {
								//提示取照片的dialog
								if (progressDialog.isShowing()
										&& progressDialog != null) {
									progressDialog.dismiss();
								}

								// 回收bitmap，并每次加入时将最后一个移除（让横向ListView每次最多只显示6个）
								if (count >= 6) {
									Photo deletePhoto = items.get(5);
									Bitmap deleteBitmap = deletePhoto
											.getBitmap();
									if (deleteBitmap != null
											&& !deleteBitmap.isRecycled()) {
										deleteBitmap.recycle();
										Log.i("PhotoFragment", "recycle!!");
									}
									hListViewAdapter.remove(deletePhoto);
								}

								photo = new Photo(bitmap);
								previewImg.setImageBitmap(bitmap);
								hListViewAdapter.insert(photo, 0);
								count++;
								Log.i("loadImage", "——>count:" + count);
							}
						}
					});
		}
	}
}