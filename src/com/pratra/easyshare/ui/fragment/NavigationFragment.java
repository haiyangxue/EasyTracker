package com.pratra.easyshare.ui.fragment;

import java.util.ArrayList;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeOption;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.CommonParams.NL_Net_Mode;
import com.baidu.navisdk.CommonParams.Const.ModelName;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.MapParams.Const.LayerMode;
import com.baidu.navisdk.comapi.routeguide.RouteGuideParams.RGLocationMode;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.routeplan.IRouteResultObserver;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.comapi.setting.SettingParams;
import com.baidu.navisdk.model.NaviDataEngine;
import com.baidu.navisdk.model.RoutePlanModel;
import com.baidu.navisdk.model.datastruct.RoutePlanNode;
import com.baidu.navisdk.ui.routeguide.BNavConfig;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.util.common.PreferenceHelper;
import com.baidu.navisdk.util.common.ScreenUtil;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.pratra.easyshare.bean.Location;
import com.pratra.easyshare.R;
import com.pratra.easyshare.httpapi.ApiHttpClient;
import com.pratra.easyshare.httpapi.Urls;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;
import com.pratra.easyshare.ui.MainActivity;
import com.pratra.easyshare.ui.baidu.BNavigatorActivity;
import com.pratra.easyshare.util.LocationUtil;

import android.content.Intent;
import android.os.Build;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

/**
 * 导航Fragment的界面
 * 
 */
public class NavigationFragment extends Fragment { 
	
	View view;
	private RoutePlanModel mRoutePlanModel = null;
	private MapGLSurfaceView mMapView = null;
//	GeoCoder startSearch = null; // 由地址到经纬度的搜索模块
//	GeoCoder endSearch = null; // 由地址到经纬度的搜索模块
	ArrayList<Location> locations=new ArrayList<Location>();
	
	private Button onlineCalcBtn, simulateNavigationBtn, realNavigationBtn;
	private ImageButton exchangePlaceBtn;
	private EditText startPlace;
	private EditText endPlace;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		view = inflater.inflate(R.layout.fragment_navigation, container, false);
		
//		// 初始化搜索模块，注册事件监听
//		startSearch = GeoCoder.newInstance();
//		startSearch.setOnGetGeoCodeResultListener(startPlaceGeo);
//		endSearch = GeoCoder.newInstance();
//		endSearch.setOnGetGeoCodeResultListener(endPlaceGeo);
		
		startPlace = (EditText) view.findViewById(R.id.start_place);
		endPlace = (EditText) view.findViewById(R.id.end_place);
		
		exchangePlaceBtn = (ImageButton)view.findViewById(R.id.exchange_place_btn);
		exchangePlaceBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				String start = startPlace.getText().toString();
				String end = endPlace.getText().toString();
				
				String temp = start;
				start = end;
				end = temp;
				
				startPlace.setText(start);
				endPlace.setText(end);
			}
		});
		
		onlineCalcBtn = (Button)view.findViewById(R.id.online_calc_btn);
		simulateNavigationBtn = (Button)view.findViewById(R.id.simulate_navigation_btn);
		realNavigationBtn = (Button)view.findViewById(R.id.real_navigation_btn);
		onlineCalcBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				calculatecRoute(NL_Net_Mode.NL_Net_Mode_OnLine);
			}
		});
		simulateNavigationBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				startNavi(false);
			}
		});
		realNavigationBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				 PreferenceHelper.getInstance(view.getContext()).putBoolean(SettingParams.Key.SP_TRACK_LOCATE_GUIDE,false);
				 startNavi(true);
			}
		});
		
		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onPause() {
		super.onPause();
		BNRoutePlaner.getInstance().setRouteResultObserver(null);
		((ViewGroup) (view.findViewById(R.id.mapview_layout))).removeAllViews();
		BNMapController.getInstance().onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		initMapView();
		((ViewGroup) (view.findViewById(R.id.mapview_layout))).addView(mMapView);
		BNMapController.getInstance().onResume();
	}

	private void initMapView() {
		if (Build.VERSION.SDK_INT < 14) {
			BaiduNaviManager.getInstance().destroyNMapView();
		}

		mMapView = BaiduNaviManager.getInstance().createNMapView(view.getContext());
		BNMapController.getInstance().setLevel(14);
		BNMapController.getInstance().setLayerMode(
				LayerMode.MAP_LAYER_MODE_BROWSE_MAP);
		updateCompassPosition();

		BNMapController.getInstance().locateWithAnimation(
				(int) (117.12218 * 1e5), (int) (36.68108 * 1e5));
	}

	/**
	 * 更新指南针位置
	 */
	private void updateCompassPosition() {
		int screenW = this.getResources().getDisplayMetrics().widthPixels;
		BNMapController.getInstance().resetCompassPosition(
				screenW - ScreenUtil.dip2px(view.getContext(), 30),
				ScreenUtil.dip2px(view.getContext(), 126), -1);
	}

	private void calculatecRoute(int netmode) {
		locations.clear();
		GeoCoder startSearch = null; // 由地址到经纬度的搜索模块
		GeoCoder endSearch = null; // 由地址到经纬度的搜索模块
		
		// 初始化搜索模块，注册事件监听
		startSearch = GeoCoder.newInstance();
		startSearch.setOnGetGeoCodeResultListener(startPlaceGeo);
		endSearch = GeoCoder.newInstance();
		endSearch.setOnGetGeoCodeResultListener(endPlaceGeo);
				
		//获取输入的起终点
		String start = startPlace.getText().toString();
		String end = endPlace.getText().toString();
		startSearch.geocode(new GeoCodeOption().city("济南").address(start));
		endSearch.geocode(new GeoCodeOption().city("济南").address(end));

	}

	private void startNavi(boolean isReal) {
		if (mRoutePlanModel == null) {
			Toast.makeText(NavigationFragment.this.getActivity(), "请先计算路径！", Toast.LENGTH_LONG).show();
			return;
		}
		// 获取路线规划结果起点
		RoutePlanNode startNode = mRoutePlanModel.getStartNode();
		// 获取路线规划结果终点
		RoutePlanNode endNode = mRoutePlanModel.getEndNode();
		if (null == startNode || null == endNode) {
			return;
		}
		// 获取路线规划算路模式
		int calcMode = BNRoutePlaner.getInstance().getCalcMode();
		Bundle bundle = new Bundle();
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_VIEW_MODE,
				BNavigator.CONFIG_VIEW_MODE_INFLATE_MAP);
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_DONE,
				BNavigator.CONFIG_CLACROUTE_DONE);
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_X,
				startNode.getLongitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_Y,
				startNode.getLatitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_X, endNode.getLongitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_Y, endNode.getLatitudeE6());
		bundle.putString(BNavConfig.KEY_ROUTEGUIDE_START_NAME,
				mRoutePlanModel.getStartName(NavigationFragment.this.getActivity(), false));
		bundle.putString(BNavConfig.KEY_ROUTEGUIDE_END_NAME,
				mRoutePlanModel.getEndName(NavigationFragment.this.getActivity(), false));
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_MODE, calcMode);
		if (!isReal) {
			// 模拟导航
			bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_LOCATE_MODE,
					RGLocationMode.NE_Locate_Mode_RouteDemoGPS);
		} else {
			// GPS 导航
			bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_LOCATE_MODE,
					RGLocationMode.NE_Locate_Mode_GPS);
		}
		
		Intent intent = new Intent(NavigationFragment.this.getActivity(), BNavigatorActivity.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	private IRouteResultObserver mRouteResultObserver = new IRouteResultObserver() {

		public void onRoutePlanYawingSuccess() {
			// TODO Auto-generated method stub

		}

		public void onRoutePlanYawingFail() {
			// TODO Auto-generated method stub

		}

		public void onRoutePlanSuccess() {
			// TODO Auto-generated method stub
			BNMapController.getInstance().setLayerMode(LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
			mRoutePlanModel = (RoutePlanModel) NaviDataEngine.getInstance().getModel(ModelName.ROUTE_PLAN);
		}

		public void onRoutePlanFail() {
			// TODO Auto-generated method stub
		}

		public void onRoutePlanCanceled() {
			// TODO Auto-generated method stub
		}

		public void onRoutePlanStart() {
			// TODO Auto-generated method stub

		}

	};
	
	OnGetGeoCoderResultListener startPlaceGeo = new OnGetGeoCoderResultListener(){

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			// TODO Auto-generated method stub
			int sX=0, sY=0, eX=0, eY=0;
			Location location=new Location();
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(NavigationFragment.this.getActivity(), "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
				return;
			}
			
			String strInfo = String.format("start纬度：%f start经度：%f",result.getLocation().latitude, result.getLocation().longitude);
			Toast.makeText(NavigationFragment.this.getActivity(), strInfo, Toast.LENGTH_LONG).show();
			
			location.setLatitude(result.getLocation().latitude);
			location.setLongitude(result.getLocation().longitude);
			locations.add(location);
////			Log.i("aaaaaaaaaaa", "aaaaaaaaaaaa  b"+locations.size());
//			if(locations.size()>=2){
//				Location start=locations.get(0);
//				Location end=locations.get(1);
//				sX = (int) (start.getLatitude()* 1e5);
//				sY = (int) (start.getLongitude()* 1e5);
//				eX = (int) (end.getLatitude()* 1e5);
//				eY = (int) (end.getLongitude()* 1e5);
//				
//				
//				//起点
//				RoutePlanNode startNode = new RoutePlanNode(sX, sY, RoutePlanNode.FROM_MAP_POINT, "华侨城", "华侨城");
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa5  "+locations.size());
//				//终点
//				RoutePlanNode endNode = new RoutePlanNode(eX, eY, RoutePlanNode.FROM_MAP_POINT, "滨海苑", "滨海苑");
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa6  "+locations.size());
//				
//				
//				//将起终点添加到nodeList
//				ArrayList<RoutePlanNode> nodeList = new ArrayList<RoutePlanNode>(2);
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa7  "+locations.size());
//				nodeList.add(startNode);
//				nodeList.add(endNode);
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa8  "+locations.size());
//				
//				
//				BNRoutePlaner.getInstance().setObserver(new RoutePlanObserver(NavigationFragment.this.getActivity(), null));
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa9  "+locations.size());
//				//设置算路方式
//				BNRoutePlaner.getInstance().setCalcMode(NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME);
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa10  "+locations.size());
//				// 设置算路结果回调
//				BNRoutePlaner.getInstance().setRouteResultObserver(mRouteResultObserver);
////				Log.i("AAAAAAAAAAA", "aaaaaaaaa11  "+locations.size());
//				// 设置起终点并算路
//				boolean ret = BNRoutePlaner.getInstance().setPointsToCalcRoute(nodeList, NL_Net_Mode.NL_Net_Mode_OnLine);
//				if(!ret){
//					Toast.makeText(NavigationFragment.this.getActivity(), "规划失败", Toast.LENGTH_SHORT).show();
//				}
//			
//			}
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};
	OnGetGeoCoderResultListener endPlaceGeo = new OnGetGeoCoderResultListener(){

		@Override
		public void onGetGeoCodeResult(GeoCodeResult result) {
			// TODO Auto-generated method stub
			int sX=0, sY=0, eX=0, eY=0;
			Location location=new Location();
			if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
				Toast.makeText(NavigationFragment.this.getActivity(), "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
				return;
			}
			
			String strInfo = String.format("end纬度：%f end经度：%f",result.getLocation().latitude, result.getLocation().longitude);
			Toast.makeText(NavigationFragment.this.getActivity(), strInfo, Toast.LENGTH_LONG).show();
			
			location.setLatitude(result.getLocation().latitude);
			location.setLongitude(result.getLocation().longitude);
			locations.add(location);

			if(locations.size()>=2){
				Location start=locations.get(0);
				Location end  =locations.get(1);
				sX = (int) (start.getLatitude()* 1e5);
				sY = (int) (start.getLongitude()* 1e5);
				eX = (int) (end.getLatitude()* 1e5);
				eY = (int) (end.getLongitude()* 1e5);

				//起点,终点
				RoutePlanNode startNode = new RoutePlanNode(sX, sY, RoutePlanNode.FROM_MAP_POINT, startPlace.getText().toString(), startPlace.getText().toString());//"华侨城"
				RoutePlanNode endNode = new RoutePlanNode(eX, eY, RoutePlanNode.FROM_MAP_POINT, endPlace.getText().toString(), endPlace.getText().toString());//"滨海苑"
				
				//将起终点添加到nodeList
				ArrayList<RoutePlanNode> nodeList = new ArrayList<RoutePlanNode>(2);
				nodeList.add(startNode);
				nodeList.add(endNode);
				BNRoutePlaner.getInstance().setObserver(new RoutePlanObserver(NavigationFragment.this.getActivity(), null));
				//设置算路方式
				BNRoutePlaner.getInstance().setCalcMode(NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TIME);
				// 设置算路结果回调
				BNRoutePlaner.getInstance().setRouteResultObserver(mRouteResultObserver);
				// 设置起终点并算路
				boolean ret = BNRoutePlaner.getInstance().setPointsToCalcRoute(nodeList,NL_Net_Mode.NL_Net_Mode_OnLine);
				if(!ret){
					Toast.makeText(NavigationFragment.this.getActivity(), "规划失败", Toast.LENGTH_SHORT).show();
				}
			}
		}

		@Override
		public void onGetReverseGeoCodeResult(ReverseGeoCodeResult arg0) {
			// TODO Auto-generated method stub
			
		}
		
	};

}