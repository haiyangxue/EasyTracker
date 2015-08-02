package com.pratra.easyshare.ui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Toast;

import com.baidu.lbsapi.auth.LBSAuthManagerListener;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.navi.location.al;
import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.BNaviEngineManager.NaviEngineInitListener;
import com.baidu.navisdk.util.verify.BNKeyVerifyListener;
import com.pratra.easyshare.R;
import com.pratra.easyshare.service.AlarmService;
import com.pratra.easyshare.ui.baidu.LocationApplication;
import com.pratra.easyshare.ui.fragment.DialFragment;
import com.pratra.easyshare.ui.fragment.HomePageFragment;
import com.pratra.easyshare.ui.fragment.LocationFragment;
import com.pratra.easyshare.ui.fragment.MessageFragment;
import com.pratra.easyshare.ui.fragment.NavigationDrawerFragment;
import com.pratra.easyshare.ui.fragment.NavigationFragment;
import com.pratra.easyshare.ui.fragment.PhotoFragment;
import com.pratra.easyshare.ui.fragment.SettingFragment;
import com.pratra.easyshare.ui.fragment.TrackFragment;

public class MainActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	/**
	 * 各个界面的Fragment
	 */
	private HomePageFragment homePageFragment = new HomePageFragment();
	private DialFragment dialFragment = new DialFragment();
	private MessageFragment messageFragment = new MessageFragment();
	private NavigationFragment navigationFragment = new NavigationFragment();
	private TrackFragment trackFragment = new TrackFragment();
	private PhotoFragment photoFragment = new PhotoFragment();
	private SettingFragment settingFragment = new SettingFragment();

	private NavigationDrawerFragment mNavigationDrawerFragment;
	// 定义一个BroadcastReceivr，接收来自service的信息
	public NotificationReceiver notificationReceiver = null;
	/**
	 * 存放上次显示在action bar中的title {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;
	AlarmService alarmService;
	public static final int HOMEPAGE = 1;
	public static final int PHONE = 2;
	public static final int MESSAGE = 3;
	public static final int NAVIGATION = 4;
	public static final int TRACKER = 5;
	public static final int PHOTO = 6;
	public static final int SETTING = 7;
	int targetFragment=0;
	public int currentPage;
	public int lastPage;
	private boolean mIsEngineInitSuccess = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//百度地圖初始化
		SDKInitializer.initialize(getApplicationContext());
		BaiduNaviManager.getInstance().initEngine(this, getSdcardDir(),
				mNaviEngineInitListener, new LBSAuthManagerListener() {
					public void onAuthResult(int status, String msg) {
						String str = null;
						if (0 == status) {
							str = "key校验成功!";
						} else {
							str = "key校验失败, " + msg;
						}
						Toast.makeText(MainActivity.this, str, Toast.LENGTH_LONG)
								.show();
						Log.d("校验失败>>>>>>>", str);
					}
				});
		setContentView(R.layout.activity_main);
		setOverflowShowingAlways();
		initializeFragments();

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// 设置抽屉
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));

		// 设置所监听的Action，并注册广播
		notificationReceiver = new NotificationReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(AlarmService.ACTION_BROADCAST);
		registerReceiver(notificationReceiver, filter);
		Log.i("MainActivity", "registerReceiver");

		 //绑定Service  
		Intent service = new Intent(this, AlarmService.class);
		startService(service);
	}
	private String getSdcardDir() {
		if (Environment.getExternalStorageState().equalsIgnoreCase(
				Environment.MEDIA_MOUNTED)) {
			return Environment.getExternalStorageDirectory().toString();
		}
		return null;
	}

	private NaviEngineInitListener mNaviEngineInitListener = new NaviEngineInitListener() {
		public void engineInitSuccess() {
			mIsEngineInitSuccess = true;
		}

		public void engineInitStart() {
		}

		public void engineInitFail() {
		}
	};
	
    private BNKeyVerifyListener mKeyVerifyListener = new BNKeyVerifyListener() {
		
		@Override
		public void onVerifySucc() {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "key校验成功", Toast.LENGTH_LONG).show();
		}
		
		@Override
		public void onVerifyFailed(int arg0, String arg1) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, "key校验失败", Toast.LENGTH_LONG).show();
		}
	};

	private void initializeFragments() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();

		currentPage = HOMEPAGE;
		ft.add(R.id.fragment_page, homePageFragment);
		ft.add(R.id.fragment_page, dialFragment);
		ft.add(R.id.fragment_page, messageFragment);
//		ft.add(R.id.fragment_page, navigationFragment);
//		ft.add(R.id.fragment_page, trackFragment);
		ft.add(R.id.fragment_page, photoFragment);
		ft.add(R.id.fragment_page, settingFragment);
//		ft.hide(homePageFragment);
		ft.hide(dialFragment);
		ft.hide(messageFragment);
		ft.hide(navigationFragment);
//		ft.hide(trackFragment);
		ft.hide(photoFragment);
		ft.hide(settingFragment);
		ft.commit();
		ft = null;
	}

	private void setOverflowShowingAlways() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");
			menuKeyField.setAccessible(true);
			menuKeyField.setBoolean(config, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (!mNavigationDrawerFragment.isDrawerOpen()) {
			getMenuInflater().inflate(R.menu.main, menu);
			restoreActionBar();
			return true;
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		if (featureId == Window.FEATURE_ACTION_BAR && menu != null) {
			if (menu.getClass().getSimpleName().equals("MenuBuilder")) {
				try {
					Method m = menu.getClass().getDeclaredMethod(
							"setOptionalIconsVisible", Boolean.TYPE);
					m.setAccessible(true);
					m.invoke(menu, true);
				} catch (Exception e) {
				}
			}
		}
		return super.onMenuOpened(featureId, menu);
	}

	public void hideFragment(int lastFragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();

		switch (lastFragment) {
		case 1:
			ft.hide(homePageFragment);
			break;
		case 2:
			ft.hide(dialFragment);
			break;
		case 3:
			ft.hide(messageFragment);
			break;
		case 4:
			ft.remove(navigationFragment);
//			ft.hide(navigationFragment);
			break;
		case 5:
			ft.remove(trackFragment);
//			ft.commit();
//			ft.add(R.id.fragment_page, navigationFragment);
//			ft.hide(navigationFragment);
			break;
		case 6:
			ft.hide(photoFragment);
//			ft.commit();
//			ft.add(R.id.fragment_page, trackFragment);
//			ft.hide(trackFragment);
			break;
		case 7:
			ft.hide(settingFragment);
			break;
		default:
			break;
		}

		ft.commit();
		ft = null;
	}

	public void showFragment(int currentFragment) {
		FragmentManager fragmentManager = getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();

		switch (currentFragment) {
		case 1:
			ft.show(homePageFragment);
			break;
		case 2:
			ft.show(dialFragment);
			break;
		case 3:
			ft.show(messageFragment);
			break;
		case 4:
			ft.add(R.id.fragment_page, navigationFragment);
			ft.show(navigationFragment);
			break;
		case 5:
			ft.add(R.id.fragment_page, trackFragment);
			ft.show(trackFragment);
			break;
		case 6:
			ft.show(photoFragment);
			break;
		case 7:
			ft.show(settingFragment);
			break;
		default:
			break;
		}
		ft.commit();
		ft = null;
	}

	@Override
	public void onNavigationDrawerItemSelected(String title) {

		lastPage = currentPage;
		if (title.equals("首页") && currentPage != HOMEPAGE) {
			currentPage = HOMEPAGE;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("电话") && currentPage != PHONE) {
			currentPage = PHONE;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("短信") && currentPage != MESSAGE) {
			currentPage = MESSAGE;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("导航") && currentPage != NAVIGATION) {
			currentPage = NAVIGATION;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("追踪") && currentPage != TRACKER) {
			currentPage = TRACKER;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("拍照") && currentPage != PHOTO) {
			currentPage = PHOTO;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		if (title.equals("设置") && currentPage != SETTING) {
			currentPage = SETTING;

			hideFragment(lastPage);
			showFragment(currentPage);
		}
		
		onSectionAttached(title);
	}

	public void onSectionAttached(String title) {
		mTitle = title;
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		// if (id == R.id.action_plus) {
		//
		// }
		if (id == R.id.home) {
			ActionBar actionBar = getSupportActionBar();
			actionBar.setHomeButtonEnabled(true);
			// 隐藏Action bar上的app icon
			actionBar.setDisplayShowHomeEnabled(true);
		}
		return super.onOptionsItemSelected(item);
	}

	public class NotificationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			LocationApplication myApp = ((LocationApplication) getApplicationContext());
			myApp.setStr("fuck");
			
			hideFragment(currentPage);
			currentPage=TRACKER;
			showFragment(TRACKER);

		}
	}  
	
    @Override  
    protected void onDestroy() {
    	unregisterReceiver(notificationReceiver);
        super.onDestroy();  
    }  
	private long exitTime = 0;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	 
        if (keyCode == KeyEvent.KEYCODE_BACK
                 && event.getRepeatCount() == 0) {
//        	lastPage = currentPage;
//    		if (currentPage != HOMEPAGE) {
//    			// Toast.makeText(MainActivity.this, mes,
//    			// Toast.LENGTH_SHORT).show();
//    			currentPage = HOMEPAGE;
//    			hideFragment(lastPage);
//    			showFragment(currentPage);
//    		}else{
    			 if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){   
    			        if((System.currentTimeMillis()-exitTime) > 2000){  
    			            Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();                                
    			            exitTime = System.currentTimeMillis();   
    			        } else {
    			            finish();
    			        }
    			        return true;   
    			    }
    		}
             return true;
//         }
//         return super.onKeyDown(keyCode, event);
     }
  
	
}