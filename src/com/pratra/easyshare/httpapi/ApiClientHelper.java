package com.pratra.easyshare.httpapi;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.baidu.mapapi.model.LatLng;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;

import android.content.Context;
import android.util.Log;

public class ApiClientHelper {
	public static String str="36.6810872068;117.122188809";
	public static LatLng ll = null;
	/**
	 * 获得请求的服务端数据的userAgent
	 * 
	 * @param appContext
	 * @return
	 */
	public static LatLng getLocation(Context context) {
		double d[] = new double[2];
		RequestQueue requestQueue = Volley.newRequestQueue(context);
		ApiHttpClient.getStringByGet(context, requestQueue,
				Urls.SENDING_LOCATION, new VolleyStringCallback() {
					@Override
					public void onSuccess(String result) {
						// TODO Auto-generated method
						str = result;
						Log.d("TAGstrrrrr", str);
					}
				});
		Log.d("TAGstr", str);
		if (str != null) {
			String[] st = str.split(";");
			d[0] = Double.parseDouble(st[0]);
			d[1] = Double.parseDouble(st[1]);
			ll = new LatLng(d[0], d[1]);
		}

		return ll;
	}
	
}
