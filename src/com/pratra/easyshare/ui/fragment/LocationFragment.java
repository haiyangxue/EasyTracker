package com.pratra.easyshare.ui.fragment;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.pratra.easyshare.R;
import com.pratra.easyshare.httpapi.ApiHttpClient;
import com.pratra.easyshare.httpapi.Urls;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

/**
 * 定位Fragment的界面
 * 
 * @author fankaichao
 */
public class LocationFragment extends Fragment {
	// 定位相关
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;
	BitmapDescriptor mCurrentMarker;

	MapView mMapView;
	BaiduMap mBaiduMap;

	// UI相关
	Button requestLocButton;
	boolean isFirstLoc = true;// 是否首次定位

	private Marker mMarkerA;
	private Marker mMarkerD;
	BitmapDescriptor bdA = null;
	BitmapDescriptor bdD = null;

	boolean view = true;
	static Location location;
	Bitmap bitmap = null;
	boolean visiual = false;
	GeoCoder mSearch = null;
	String addr = null;

	View viewMap;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		viewMap = inflater
				.inflate(R.layout.activity_location, container, false);

		requestLocButton = (Button) viewMap.findViewById(R.id.button1);
		mCurrentMode = LocationMode.NORMAL;
		requestLocButton.setText("定位");
		OnClickListener btnClickListener = new OnClickListener() {
			public void onClick(View v) {
				switch (mCurrentMode) {
				case NORMAL:
					// Intent intent = new Intent(LocationActivity.this,
					// RoutePlan.class);
					// startActivity(intent);
					// finish();
					requestLocButton.setText("跟随");
					mCurrentMode = LocationMode.FOLLOWING;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case COMPASS:
					requestLocButton.setText("普通");
					mCurrentMode = LocationMode.NORMAL;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				case FOLLOWING:
					requestLocButton.setText("罗盘");
					mCurrentMode = LocationMode.COMPASS;
					mBaiduMap
							.setMyLocationConfigeration(new MyLocationConfiguration(
									mCurrentMode, true, mCurrentMarker));
					break;
				}
			}
		};
		requestLocButton.setOnClickListener(btnClickListener);

		// 地图初始化
		mMapView = (MapView) viewMap.findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		
		bdA = BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);

		bdD = BitmapDescriptorFactory.fromResource(R.drawable.icon_markd);
		// 开启定位图层
		mBaiduMap.setMyLocationEnabled(true);
		// 定位初始化
		mLocClient = new LocationClient(viewMap.getContext());
		mLocClient.registerLocationListener(myListener);
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开gps
		option.setCoorType("bd09ll"); // 设置坐标类型
		option.setScanSpan(1000);
		mLocClient.setLocOption(option);
		mLocClient.start();

		return viewMap;
	}
	
	private long exitTime = 0;

	@Override
	public void onDestroy() {
		 super.onDestroy();
		// System.exit(0);
	}

	
	@Override
	public void onDetach() {
		mLocClient.stop();
		 // 关闭定位图层
		 mBaiduMap.setMyLocationEnabled(false);
		 mMapView.onDestroy();
		 mMapView = null;
		super.onDetach();
	}
	@Override
	public void onDestroyView() {
		super.onDestroyView();
	}
	@Override
	public void onResume() {
		mMapView.onResume();
		super.onResume();
	}
	@Override
	public void onPause() {
		mMapView.onPause();
		super.onPause();
	}

	protected void setButtonListener() {
	}

	



	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mMapView == null)
				return;
			Log.d("TAG", "code:" + location.getLocType());
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			RequestQueue requestQueue = Volley.newRequestQueue(viewMap.getContext());
			ApiHttpClient.getStringByGet(viewMap.getContext(), requestQueue,
					Urls.SENDING_LOCATION, new VolleyStringCallback() {
						@Override
						public void onSuccess(String result) {
							String[] st = result.split(";");
							Log.d("11111", result);
							LatLng ll=new LatLng(Double.parseDouble(st[0]),
									Double.parseDouble(st[1]));
							OverlayOptions ooA = new MarkerOptions().position(ll).icon(bdA)
									.zIndex(9).draggable(true);
							mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
							ArrayList<BitmapDescriptor> giflist = new ArrayList<BitmapDescriptor>();
							giflist.add(bdA);

							OverlayOptions ooD = new MarkerOptions().position(ll)
									.icons(giflist).zIndex(0).period(10);
							mMarkerD = (Marker) (mBaiduMap.addOverlay(ooD));

							
						}
					});
			if (isFirstLoc) {
				isFirstLoc = false;
				LatLng ll = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
				mBaiduMap.animateMapStatus(u);
				}
			}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}
}