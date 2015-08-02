package com.pratra.easyshare;

import java.io.File;
import java.util.List;

import com.pratra.easyshare.service.AlarmService;
import com.pratra.easyshare.ui.MainActivity;
import com.pratra.easyshare.util.FileUtils;
import com.pratra.easyshare.util.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.LinearLayout;

/**
 * 应用程序启动类：显示欢迎界面并跳转到主界面
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class AppStart extends Activity {

	private static final String TAG = "AppStart";
	final AppContext ac = (AppContext) getApplication();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final View view = View.inflate(this, R.layout.app_start, null);
		LinearLayout wellcome = (LinearLayout) view
				.findViewById(R.id.app_start_view);
		check(wellcome);
		setContentView(view);

		// 渐变展示启动屏
		AlphaAnimation aa = new AlphaAnimation(0.3f, 1.0f);
		aa.setDuration(2000);
		view.startAnimation(aa);
		aa.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationEnd(Animation arg0) {
				redirectTo();
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationStart(Animation animation) {
			}

		});
		// 启动服务，时刻计算Host与Remote的相隔距离，并进行适当的操作
		Intent service = new Intent(this, AlarmService.class);
		startService(service);
		Log.i("Main", "start service");

	}

	/**
	 * 检查是否需要换图片
	 * 
	 * @param view
	 */
	private void check(LinearLayout view) {
		String path = FileUtils.getAppCache(this, "welcomeback");
		List<File> files = FileUtils.listPathFiles(path);
		if (!files.isEmpty()) {
			File f = files.get(0);
			long time[] = getTime(f.getName());
			long today = StringUtils.getToday();
			if (today >= time[0] && today <= time[1]) {
				view.setBackgroundDrawable(Drawable.createFromPath(f
						.getAbsolutePath()));
			}
		}
	}

	/**
	 * 分析显示的时间
	 * 
	 * @param time
	 * @return
	 */
	private long[] getTime(String time) {
		long res[] = new long[2];
		try {
			time = time.substring(0, time.indexOf("."));
			String t[] = time.split("-");
			res[0] = Long.parseLong(t[0]);
			if (t.length >= 2) {
				res[1] = Long.parseLong(t[1]);
			} else {
				res[1] = Long.parseLong(t[0]);
			}
		} catch (Exception e) {
		}
		return res;
	}

	/**
	 * 跳转到...
	 */
	private void redirectTo() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
}