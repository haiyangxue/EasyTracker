package com.pratra.easyshare.util;

import java.text.DecimalFormat;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

public class LocationUtil {
	public static final double MIN_DIS = 0.0001;
	private static Context mContext;
	private static LocationManager locationManager;

	/**
	 * 获取地址的经纬度　　
	 * 
	 * @return 纬度_经度_高度
	 */
	public static String getGpsAddress(Context context) {
		// 返回所有已知的位置提供者的名称列表，包括未获准访问或调用活动目前已停用的。
		locationManager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		criteria.setCostAllowed(false);
		// 设置位置服务免费
		criteria.setAccuracy(Criteria.ACCURACY_COARSE); // 设置水平位置精度
		// getBestProvider 只有允许访问调用活动的位置供应商将被返回
		String providerName = locationManager.getBestProvider(criteria, true);

		if (providerName != null) {
			Location location = locationManager
					.getLastKnownLocation(providerName);
			if (location != null)
				return location.getLatitude() + ";" + location.getLongitude()
						+ ";" + location.getAltitude();
		} else {
			Toast.makeText(mContext, "1.请检查网络连接 \n2.请打开我的位置",
					Toast.LENGTH_SHORT).show();
		}
		return null;
	}

	// 计算方位角pab。
	public static double gps2d(double lat_a, double lng_a, double lat_b,
			double lng_b) {
		double d = 0;
		lat_a = lat_a * Math.PI / 180;
		lng_a = lng_a * Math.PI / 180;
		lat_b = lat_b * Math.PI / 180;
		lng_b = lng_b * Math.PI / 180;

		d = Math.sin(lat_a) * Math.sin(lat_b) + Math.cos(lat_a)
				* Math.cos(lat_b) * Math.cos(lng_b - lng_a);
		d = Math.sqrt(1 - d * d);
		d = Math.cos(lat_b) * Math.sin(lng_b - lng_a) / d;
		d = Math.asin(d) * 180 / Math.PI;

		// d = Math.round(d*10000);
		return d;
	}

	// 给出方向
	public static String showBearing(double lat_a, double lng_a, double lat_b,
			double lng_b) {

		double heading = gps2d(lat_a, lng_a, lat_b, lng_b);

		DecimalFormat df = new DecimalFormat("#.00");

		if (heading < 0) {
			heading = heading + 360;
		}

		String s = null;
		if (Math.abs(heading) <= MIN_DIS) {
			s = new String("正北方向上");
			return s;
		}

		if (Math.abs(heading - 90) <= MIN_DIS) {
			s = new String("正东方向上");
			return s;
		}

		if (Math.abs(heading - 180) <= MIN_DIS) {
			s = new String("正南方向上");
			return s;
		}

		if (Math.abs(heading - 270) <= MIN_DIS) {
			s = new String("正西方向上");
			return s;
		}

		if ((heading) > MIN_DIS && (90 - heading) > MIN_DIS
				&& Math.abs(heading - 45) > MIN_DIS) {
			s = new String("北偏东" + df.format(heading) + "度方向上");
			return s;
		}
		if (Math.abs(heading - 45) <= MIN_DIS) {
			s = new String("东北方向上");
			return s;
		}

		if ((heading - 90) > MIN_DIS && (180 - heading) > MIN_DIS
				&& Math.abs(heading - 135) > MIN_DIS) {
			s = new String("南偏东" + df.format(180 - heading) + "度方向上");
			return s;
		}
		if (Math.abs(heading - 135) <= MIN_DIS) {
			s = new String("东南方向上");
			return s;
		}

		if ((heading - 180) > MIN_DIS && (270 - heading) > MIN_DIS
				&& Math.abs(heading - 225) > MIN_DIS) {
			s = new String("南偏西" + df.format(heading - 180) + "度方向上");
			return s;
		}
		if (Math.abs(heading - 225) <= MIN_DIS) {
			s = new String("西南方向上");
			return s;
		}

		if ((heading - 270) > MIN_DIS && (360 - heading) > MIN_DIS
				&& Math.abs(heading - 315) > MIN_DIS) {
			s = new String("北偏西" + df.format(360 - heading) + "度方向上");
			return s;
		}
		if (Math.abs(heading - 315) <= MIN_DIS) {
			s = new String("西北方向上");
			return s;
		}

		return s;
	}
}
