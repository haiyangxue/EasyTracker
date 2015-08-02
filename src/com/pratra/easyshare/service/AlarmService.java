package com.pratra.easyshare.service;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.pratra.easyshare.R;
import com.pratra.easyshare.httpapi.ApiHttpClient;
import com.pratra.easyshare.httpapi.Urls;
import com.pratra.easyshare.httpapi.ApiHttpClient.VolleyStringCallback;
import com.pratra.easyshare.ui.MainActivity;
import com.pratra.easyshare.ui.baidu.LocationApplication;
import com.pratra.easyshare.ui.fragment.TrackFragment;
import com.pratra.easyshare.util.DistanceUtil;
import com.pratra.easyshare.util.LocationUtil;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.MenuItem;

public class AlarmService extends Service {
	// 自定义一个距离的阈值，用于测试
	public static final int MAX_DISTANCE = 300;
	// 假定一个计算出来的距离
	public int distance = -1;
	public Handler handler;
	public static Timer timer;
	public static TimerTask task;
	RequestQueue requestQueue;
	public static final String ACTION_BROADCAST = "com.pratra.action.ACTION_BROADCAST";
	LocationApplication myApp;

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("SERVICE", "service start aaaaaaaaaaaaaaaaaaa");
		distance = 50;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
//		return startId;
//		showNotification(distance);
		myApp = ((LocationApplication) getApplicationContext());
		requestQueue = Volley.newRequestQueue(getApplicationContext());
		handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ApiHttpClient.getStringByGet(getApplicationContext(),
							requestQueue, Urls.SENDING_LOCATION,
							new VolleyStringCallback() {
								@Override
								public void onSuccess(String result) {
									// TODO Auto-generated method
//									double lon1, lat1, lon2, lat2;
//									/*分割之后，纬度/经度/高度*/
//									String[] st = result.split(";");
//									lon1 = Double.parseDouble(st[1]);
//									lat1 = Double.parseDouble(st[0]);
//									String[] st2 = LocationUtil.getGpsAddress(
//											getApplicationContext()).split(";");
//									lon2 = Double.parseDouble(st2[1]);
//									lat2 = Double.parseDouble(st2[0]);
//									distance = (int) DistanceUtil
//											.getShortDistance(lon1, lat1, lon2,
//													lat2);
									if (distance <= MAX_DISTANCE) {
										myApp.setStr("pratra");
										// return -1;
									} else {
										if (!myApp.getStr().equals("fuck")) {
											showNotification(distance);
										}
									}
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
				// Log.d("TAG", "333");
				Message message = new Message();
				message.what = 1;
				handler.sendMessage(message);

			}
		};
		// 1s后执行task,经过1s再次执行
		timer.schedule(task, 0, 1000);

		return super.onStartCommand(intent, flags, startId);
	}

	int getDistance() {

		return distance;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	private void showNotification(int farAway) {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// 实例化通知栏构造器NotificationCompat.Builder
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
				this);

		mBuilder.setContentTitle("警报！警报！")// 设置通知栏标题
				// 设置通知的内容
				.setContentText("您的物品距离您有" + farAway + "m")
				// 通知首次出现在通知栏，带上升动画效果的
				.setTicker("您的通知来了")
				// 通知产生的时间，会在通知信息里显示，一般是系统获取到的时间
				.setWhen(System.currentTimeMillis())
				// 设置这个标志当用户单击面板就可以让通知将自动取消
				.setAutoCancel(true)
				// 向通知添加声音、闪灯和振动效果
				.setDefaults(Notification.DEFAULT_ALL)
				// 设置三色灯提醒
				// .setLights(0xff0000ff, 300, 0)
				// 设置震动方式
				// 设置声音提醒
				// .setSound(
				// Uri.withAppendedPath(
				// Audio.Media.INTERNAL_CONTENT_URI, "5"))
				// 设置为最高优先级别
				.setPriority(Notification.PRIORITY_MAX)
				// 设置图标
				.setSmallIcon(R.drawable.notify);

		Intent intent = new Intent(ACTION_BROADCAST);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0,
				intent, PendingIntent.FLAG_UPDATE_CURRENT);
		mBuilder.setContentIntent(pendingIntent);
		// 发送通知
		notificationManager.notify(1, mBuilder.build());

		// 并将主界面上的铃铛设置为红色的
		// TrackFragment.item
		// MenuItem bell =
		// (MenuItem)MainActivity.findViewById(R.id.action_notify);
 	}

	/**
	 * 返回一个Binder对象
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return new MsgBinder();
	}

	public class MsgBinder extends Binder {
		/**
		 * 获取当前Service的实例
		 * 
		 * @return
		 */
		public AlarmService getService() {
			return AlarmService.this;
		}
	}

}
