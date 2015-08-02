package com.pratra.easyshare.ui.fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.pratra.easyshare.R;
import com.pratra.easyshare.httpapi.ApiHttpClient;
import com.pratra.easyshare.httpapi.Urls;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;
import com.pratra.easyshare.util.DistanceUtil;
import com.pratra.easyshare.util.LocationUtil;

import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
public class TrackFragment extends Fragment {
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

	LatLng ll_start;
	LatLng ll_end;

	boolean view = true;
	static Location location;
	Bitmap bitmap = null;
	boolean visiual = false;
	GeoCoder mSearch = null;
	String addr = null;

	View viewMap;

	public Handler handler;
	public static Timer timer;
	public static TimerTask task;
	RequestQueue requestQueue;

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
		getMarkerLocation();
		return viewMap;
	}

	public void getMarkerLocation() {

		final RequestQueue requestQueue = Volley.newRequestQueue(viewMap
				.getContext());
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ApiHttpClient.getStringByGet(viewMap.getContext(),
							requestQueue, Urls.SENDING_LOCATION,
							new VolleyStringCallback() {
								@Override
								public void onSuccess(String result) {
									/* result为remote传送过来的地理位置信息的拼接字符串 */
									if (mMapView != null) {
										// 记录Host，Remote的经纬度地址信息
										double lon1, lat1, lon2, lat2;

										mBaiduMap.clear();
										/* 分割之后，纬度/经度/高度 */
										String[] st = result.split(";");
										Log.d("11111", "aaaaaaaaa" + result);

										lon1 = Double.parseDouble(st[1]);
										lat1 = Double.parseDouble(st[0]);

										/* LatLng是以纬度和经度表示的地理坐标点 */
										// ll_end = new LatLng(Double
										// .parseDouble(st[0]), Double
										// .parseDouble(st[1]));
										ll_end = new LatLng(lat1, lon1);

										OverlayOptions ooA = new MarkerOptions()
												.position(ll_end).icon(bdA)
												.zIndex(9).draggable(true);
										mMarkerA = (Marker) (mBaiduMap
												.addOverlay(ooA));

										/* 分割之后，纬度/经度/高度 */
										String[] st2 = LocationUtil
												.getGpsAddress(
														TrackFragment.this
																.getActivity())
												.split(";");

										lon2 = Double.parseDouble(st2[1]);
										lat2 = Double.parseDouble(st2[0]);

										ll_start = new LatLng(lat2, lon2);
										// ll_start = new LatLng(Double
										// .parseDouble(st2[0]), Double
										// .parseDouble(st2[1]));

										// 计算距离，方位角
										double distance = (int) DistanceUtil
												.getShortDistance(lon1, lat1,
														lon2, lat2);
										Log.i("两者的距离:", "——>" + distance);
										
										String angle = LocationUtil.showBearing(lat1, lon1, lat2, lon2);
										Log.i("方向为:", angle);
									}
									// 添加折线
									// LatLng p1 = ll_start;

									// LatLng p2 = ll_end;
									// LatLng p3 = ll_end;
									// List<LatLng> points = new
									// ArrayList<LatLng>();
									// points.add(p1);
									// points.add(p2);
									// points.add(p3);
									//
									// OverlayOptions ooPolyline = new
									// PolylineOptions().width(10)
									// .color(0xAAFF0000).points(points);
									// mBaiduMap.addOverlay(ooPolyline);

								}
							});
				}
				super.handleMessage(msg);
			};
		};

		timer = new Timer();
		task = new TimerTask() {
			@Override
			public void run() {
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);

			}
		};
		// 1s后执行task,经过1s再次执行
		timer.schedule(task, 0, 1000);
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
		// mBaiduMap.clear();
		mMapView.onDestroy();
		mMapView = null;
		Log.i("aa", "onDestroyaaaaaaaaaaaaaaaaa");
		timer.cancel();
		task.cancel();
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

			if (isFirstLoc) {
				RequestQueue requestQueue = Volley.newRequestQueue(viewMap
						.getContext());
				ApiHttpClient.getStringByGet(viewMap.getContext(),
						requestQueue, Urls.SENDING_LOCATION,
						new VolleyStringCallback() {
							@Override
							public void onSuccess(String result) {
								mBaiduMap.clear();
								String[] st = result.split(";");
								Log.d("11111", "aaaaaaaaa" + result);
								ll_end = new LatLng(Double.parseDouble(st[0]),
										Double.parseDouble(st[1]));
								OverlayOptions ooA = new MarkerOptions()
										.position(ll_end).icon(bdA).zIndex(9)
										.draggable(true);
								mMarkerA = (Marker) (mBaiduMap.addOverlay(ooA));
							}
						});
				isFirstLoc = false;
				ll_end = new LatLng(location.getLatitude(),
						location.getLongitude());
				MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll_end);
				mBaiduMap.animateMapStatus(u);
			}
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

}

// package com.pratra.easyshare.ui.fragment;
//
// import java.util.ArrayList;
//
// import com.android.volley.RequestQueue;
// import com.android.volley.toolbox.Volley;
// import com.baidu.lbsapi.auth.LBSAuthManagerListener;
// import com.baidu.navisdk.BaiduNaviManager;
// import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
// import com.baidu.navisdk.CommonParams.NL_Net_Mode;
// import com.baidu.navisdk.CommonParams.Const.ModelName;
// import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
// import com.baidu.navisdk.comapi.mapcontrol.MapParams.Const.LayerMode;
// import com.baidu.navisdk.comapi.routeguide.RouteGuideParams.RGLocationMode;
// import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
// import com.baidu.navisdk.comapi.routeplan.IRouteResultObserver;
// import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
// import com.baidu.navisdk.comapi.setting.SettingParams;
// import com.baidu.navisdk.model.NaviDataEngine;
// import com.baidu.navisdk.model.RoutePlanModel;
// import com.baidu.navisdk.model.datastruct.RoutePlanNode;
// import com.baidu.navisdk.ui.routeguide.BNavConfig;
// import com.baidu.navisdk.ui.routeguide.BNavigator;
// import com.baidu.navisdk.ui.widget.RoutePlanObserver;
// import com.baidu.navisdk.util.common.PreferenceHelper;
// import com.baidu.navisdk.util.common.ScreenUtil;
// import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
// import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
// import com.pratra.easyshare.R;
// import com.pratra.easyshare.httpapi.ApiHttpClient;
// import com.pratra.easyshare.httpapi.Urls;
// import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;
// import com.pratra.easyshare.ui.baidu.BNavigatorActivity;
// import com.pratra.easyshare.util.LocationUtil;
//
// import android.content.Intent;
// import android.os.Build;
// import android.os.Bundle;
// import android.os.Environment;
// import android.support.v4.app.Fragment;
// import android.util.DisplayMetrics;
// import android.util.Log;
// import android.util.TypedValue;
// import android.view.Gravity;
// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.view.View.OnClickListener;
// import android.widget.FrameLayout;
// import android.widget.TextView;
// import android.widget.Toast;
// import android.widget.FrameLayout.LayoutParams;
//
// /**
// * 追踪Fragment的界面
// *
// */
// public class TrackFragment extends Fragment {
// private RoutePlanModel mRoutePlanModel = null;
// private MapGLSurfaceView mMapView = null;
//
// View view;
// @Override
// public View onCreateView(LayoutInflater inflater, ViewGroup container,
// Bundle savedInstanceState) {
//
// view = inflater.inflate(R.layout.activity_routeplan, container, false);
//
//
// startCalcRoute(NL_Net_Mode.NL_Net_Mode_OnLine);
// // view.findViewById(R.id.).setOnClickListener(
// // new OnClickListener() {
// //
// // public void onClick(View arg0) {
// // startCalcRoute(NL_Net_Mode.NL_Net_Mode_OnLine);
// // }
// // });
//
// // view.findViewById(R.id.simulate_btn).setOnClickListener(
// // new OnClickListener() {
// //
// // public void onClick(View arg0) {
// // startNavi(false);
// // }
// // });
//
// // view.findViewById(R.id.real_btn).setOnClickListener(new OnClickListener()
// {
// //
// // public void onClick(View arg0) {
// // PreferenceHelper.getInstance(view.getContext())
// // .putBoolean(SettingParams.Key.SP_TRACK_LOCATE_GUIDE,
// // false);
// // startNavi(true);
// // }
// // });
//
// return view;
// }
//
//
//
// @Override
// public void onDestroy() {
// super.onDestroy();
// }
//
// @Override
// public void onPause() {
// super.onPause();
// BNRoutePlaner.getInstance().setRouteResultObserver(null);
// ((ViewGroup) (view.findViewById(R.id.mapview_layout))).removeAllViews();
// BNMapController.getInstance().onPause();
// }
//
// @Override
// public void onResume() {
// super.onResume();
// initMapView();
// ((ViewGroup) (view.findViewById(R.id.mapview_layout))).addView(mMapView);
// BNMapController.getInstance().onResume();
// }
//
// private void initMapView() {
// if (Build.VERSION.SDK_INT < 14) {
// BaiduNaviManager.getInstance().destroyNMapView();
// }
//
// mMapView = BaiduNaviManager.getInstance().createNMapView(view.getContext());
// BNMapController.getInstance().setLevel(14);
// BNMapController.getInstance().setLayerMode(
// LayerMode.MAP_LAYER_MODE_BROWSE_MAP);
// updateCompassPosition();
//
// BNMapController.getInstance().locateWithAnimation(
// (int) (117.12218 * 1e5), (int) (36.68108 * 1e5));
// }
//
// /**
// * 更新指南针位置
// */
// private void updateCompassPosition() {
// int screenW = this.getResources().getDisplayMetrics().widthPixels;
// BNMapController.getInstance().resetCompassPosition(
// screenW - ScreenUtil.dip2px(view.getContext(), 30),
// ScreenUtil.dip2px(view.getContext(), 126), -1);
// }
//
// private void startCalcRoute(int netmode) {
// RequestQueue requestQueue = Volley.newRequestQueue(view.getContext());
// ApiHttpClient.getStringByGet(view.getContext(), requestQueue,
// Urls.SENDING_LOCATION, new VolleyStringCallback() {
// @Override
// public void onSuccess(String result) {
// // TODO Auto-generated method
// int sX = 0, sY = 0, eX = 0, eY = 0;
// String startL="36;117";
// String[] end = result.split(";");
// if(LocationUtil.getGpsAddress(view.getContext())!=null){
// startL=LocationUtil.getGpsAddress(view.getContext());}
// String[] start=startL.split(";");;
// sX=(int) (Double.parseDouble(start[0])*100000);
// sY=(int) (Double.parseDouble(start[1])*100000);
// eX=(int) (Double.parseDouble(end[0])*100000);
// eY=(int) (Double.parseDouble(end[1])*100000);
// Log.d("TARGERT", "aaaaaaaaaaaaaaa"+sX+","+eX+","+sY+","+eY+",");
//
// // 起点
// RoutePlanNode startNode = new RoutePlanNode(sX, sY,
// RoutePlanNode.FROM_MAP_POINT, "华侨城", "华侨城");
// // 终点
// RoutePlanNode endNode = new RoutePlanNode(eX, eY,
// RoutePlanNode.FROM_MAP_POINT, "滨海苑", "滨海苑");
// // 将起终点添加到nodeList
// ArrayList<RoutePlanNode> nodeList = new ArrayList<RoutePlanNode>(2);
// nodeList.add(startNode);
// nodeList.add(endNode);
// BNRoutePlaner.getInstance().setObserver(new RoutePlanObserver(
// TrackFragment.this.getActivity(), null));
// // 设置算路方式
// BNRoutePlaner.getInstance().setCalcMode(
// NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME);
// // 设置算路结果回调
// BNRoutePlaner.getInstance()
// .setRouteResultObserver(mRouteResultObserver);
// // 设置起终点并算路
// boolean ret = BNRoutePlaner.getInstance().setPointsToCalcRoute(
// nodeList, NL_Net_Mode.NL_Net_Mode_OnLine);
// }
// });
//
//
// }
//
// private void startNavi(boolean isReal) {
// if (mRoutePlanModel == null) {
// Toast.makeText(view.getContext(), "请先算路！", Toast.LENGTH_LONG).show();
// return;
// }
// // 获取路线规划结果起点
// RoutePlanNode startNode = mRoutePlanModel.getStartNode();
// // 获取路线规划结果终点
// RoutePlanNode endNode = mRoutePlanModel.getEndNode();
// if (null == startNode || null == endNode) {
// return;
// }
// // 获取路线规划算路模式
// int calcMode = BNRoutePlaner.getInstance().getCalcMode();
// Bundle bundle = new Bundle();
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_VIEW_MODE,
// BNavigator.CONFIG_VIEW_MODE_INFLATE_MAP);
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_DONE,
// BNavigator.CONFIG_CLACROUTE_DONE);
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_X,
// startNode.getLongitudeE6());
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_Y,
// startNode.getLatitudeE6());
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_X, endNode.getLongitudeE6());
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_Y, endNode.getLatitudeE6());
// bundle.putString(BNavConfig.KEY_ROUTEGUIDE_START_NAME,
// mRoutePlanModel.getStartName(view.getContext(), false));
// bundle.putString(BNavConfig.KEY_ROUTEGUIDE_END_NAME,
// mRoutePlanModel.getEndName(view.getContext(), false));
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_MODE, calcMode);
// if (!isReal) {
// // 模拟导航
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_LOCATE_MODE,
// RGLocationMode.NE_Locate_Mode_RouteDemoGPS);
// } else {
// // GPS 导航
// bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_LOCATE_MODE,
// RGLocationMode.NE_Locate_Mode_GPS);
// }
//
// Intent intent = new Intent(TrackFragment.this.getActivity(),
// BNavigatorActivity.class);
// intent.putExtras(bundle);
// startActivity(intent);
// }
//
// private IRouteResultObserver mRouteResultObserver = new
// IRouteResultObserver() {
//
// public void onRoutePlanYawingSuccess() {
// // TODO Auto-generated method stub
//
// }
//
// public void onRoutePlanYawingFail() {
// // TODO Auto-generated method stub
//
// }
//
// public void onRoutePlanSuccess() {
// // TODO Auto-generated method stub
// BNMapController.getInstance().setLayerMode(
// LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
// mRoutePlanModel = (RoutePlanModel) NaviDataEngine.getInstance()
// .getModel(ModelName.ROUTE_PLAN);
// }
//
// public void onRoutePlanFail() {
// // TODO Auto-generated method stub
// }
//
// public void onRoutePlanCanceled() {
// // TODO Auto-generated method stub
// }
//
// public void onRoutePlanStart() {
// // TODO Auto-generated method stub
//
// }
//
// };
// }