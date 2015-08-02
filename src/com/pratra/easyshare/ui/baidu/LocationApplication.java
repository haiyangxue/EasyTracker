package com.pratra.easyshare.ui.baidu;

import java.util.Timer;
import java.util.TimerTask;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import android.app.Application;

public class LocationApplication extends Application {
	public LocationClient mLocationClient;// 定位SDK的核心类
	public MyLocationListener mMyLocationListener;// 定义监听类
	public static String sb = null;
	TimerTask timertask;
	Timer timer;
	String str="pratra";

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationClient = new LocationClient(this.getApplicationContext());
		mMyLocationListener = new MyLocationListener();
		mLocationClient.registerLocationListener(mMyLocationListener);
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public Timer getTimer() {
		return timer;

	}

	public void setTimerTask(TimerTask timertask) {
		this.timertask = timertask;
	}

	public TimerTask getTimerTask() {
		return timertask;

	}
	public void setStr(String str) {
		this.str = str;
	}

	public String getStr() {
		return str;

	}
	/**
	 * 实现实位回调监听
	 */
	public class MyLocationListener implements BDLocationListener {

		public void onReceiveLocation(BDLocation location) {

			if (location.getLocType() == BDLocation.TypeGpsLocation) {// 通过GPS定位
				sb = location.getAddrStr();// 获得当前地址
			} else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 通过网络连接定位
				sb = location.getAddrStr();// 获得当前地址
			}

		}
	}
}
