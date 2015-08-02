package com.pratra.easyshare.httpapi;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageLoader.ImageCache;
import com.android.volley.toolbox.ImageLoader.ImageContainer;
import com.android.volley.toolbox.ImageLoader.ImageListener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.pratra.easyshare.R;
import com.pratra.easyshare.widget.CustomProgressDialog;

/**
 * 1 利用Volley获取JSON数据 2 利用Volley异步加载图片 1 利用NetworkImageView加载网路图片
 * 
 */
public class ApiHttpClient {
	private NetworkImageView mNetworkImageView;
	public static String result;
	public static RequestQueue requestQueue;
	public static ImageLoader imageLoader;
	public static ImageListener listener;

	public static LruCache<String, Bitmap> lruCache;
	public static Timer timer;
	public static TimerTask task;
	public static Handler handler;
	public static Bitmap bitm;
	public static void getStringByGet(Context context,
			RequestQueue requestQueue, String url,
			final VolleyStringCallback callback) {
		StringRequest stringRequest = new StringRequest(Request.Method.GET,
				url, new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						callback.onSuccess(response);
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.e("TAG", error.getMessage(), error);
						result = error.getMessage();
					}
				});
		requestQueue.add(stringRequest);
	}

	// public void getJsonByGet(String url) {
	// RequestQueue requestQueue = Volley.newRequestQueue(this);
	// String JSONDataUrl = url;
	// // final ProgressDialog progressDialog = ProgressDialog.show(this,
	// // "This is title", "...Loading...");
	//
	// JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
	// Request.Method.GET, JSONDataUrl, null,
	// new Response.Listener<JSONObject>() {
	// @Override
	// public void onResponse(JSONObject response) {
	// System.out.println("response=" + response);
	// if (progressDialog.isShowing()
	// && progressDialog != null) {
	// progressDialog.dismiss();
	// }
	// }
	// }, new Response.ErrorListener() {
	// @Override
	// public void onErrorResponse(VolleyError arg0) {
	// System.out.println("sorry,Error");
	// }
	// });
	// requestQueue.add(jsonObjectRequest);
	// }

	/**
	 * 利用Volley获取JSON数据
	 */
	// private void getJSONByVolley() {
	// RequestQueue requestQueue = Volley.newRequestQueue(this);
	// String JSONDataUrl =
	// "http://pipes.yahooapis.com/pipes/pipe.run?_id=giWz8Vc33BG6rQEQo_NLYQ&_render=json";
	// final ProgressDialog progressDialog = ProgressDialog.show(this,
	// "This is title", "...Loading...");
	//
	// JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
	// Request.Method.GET, JSONDataUrl, null,
	// new Response.Listener<JSONObject>() {
	// @Override
	// public void onResponse(JSONObject response) {
	// System.out.println("response=" + response);
	// if (progressDialog.isShowing()
	// && progressDialog != null) {
	// progressDialog.dismiss();
	// }
	// }
	// }, new Response.ErrorListener() {
	// @Override
	// public void onErrorResponse(VolleyError arg0) {
	// System.out.println("sorry,Error");
	// }
	// });
	// requestQueue.add(jsonObjectRequest);
	// }

	/**
	 * 加载网络图片
	 * 
	 * 注意方法参数: getImageListener(ImageView view, int defaultImageResId, int
	 * errorImageResId) 第一个参数:显示图片的ImageView 第二个参数:默认显示的图片资源 第三个参数:加载错误时显示的图片资源
	 */
	public static void loadImage(final Context context, ImageView imageview,
			int defaultimage, int failedimage,
			final VolleyImageCallback callback) {
		requestQueue = Volley.newRequestQueue(context);
		getStringByGet(context, requestQueue, Urls.TAKINGPHOTO,
				new VolleyStringCallback() {
					@Override
					public void onSuccess(String result) {
						// TODO Auto-generated method stub
					}
				});
		lruCache = new LruCache<String, Bitmap>(20);
		ImageCache imageCache = new ImageCache() {
			@Override
			public void putBitmap(String key, Bitmap value) {
				lruCache.put(key, value);
			}

			@Override
			public Bitmap getBitmap(String key) {
				return lruCache.get(key);
			}
		};
		imageLoader = new ImageLoader(requestQueue, imageCache);
		listener = ImageLoader.getImageListener(imageview, defaultimage,
				failedimage);
		
		handler = new Handler() {
			
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
//					final CustomProgressDialog progressDialog =CustomProgressDialog(context, "正在加载中",R.anim.frame);
//					progressDialog.show();
					imageLoader.get(Urls.PHOTO_URL, new ImageListener() {
						@Override
						public void onResponse(ImageContainer response,
								boolean isImmediate) {
							String imageUrl = response.getRequestUrl();
							Bitmap tbm = response.getBitmap();
							callback.onSuccess(tbm, isImmediate);
//							if (progressDialog.isShowing()&&progressDialog!=null) { 
//	                            progressDialog.dismiss(); 
//	                        } 
							if (tbm != null) {
								imageLoader.get(imageUrl, listener);
								getStringByGet(context,
										requestQueue, Urls.REQUESTEND,
										new VolleyStringCallback() {
											@Override
											public void onSuccess(String result) {
												// TODO Auto-generated method
												Log.i("onSuccess", "aaaaaaaaaaa"+result);
												if (result.equals(Urls.REQUEST_REQUEST_END)){
													Log.i("onSuccess", "aaaaaaaaaaa");
													stopTimer();
													}
											}
										});

							} else {
								Log.d("Photo", "bbbbbbbbbno");
//								stopTimer();
							}

						}

						@Override
						public void onErrorResponse(VolleyError error) {
							// TODO Auto-generated method stub
							Log.d("Photo", "bbbbbbbbbno");
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
		timer.schedule(task, 1000, 1000);

	}

	public static void stopTimer() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		if (task != null) {
			task.cancel();
			task = null;
		}
	}

	/**
	 * 利用NetworkImageView显示网络图片
	 */
	private void showImageByNetworkImageView(Context context) {
		String imageUrl = "http://avatar.csdn.net/6/6/D/1_lfdfhl.jpg";
		RequestQueue requestQueue = Volley.newRequestQueue(context);
		final LruCache<String, Bitmap> lruCache = new LruCache<String, Bitmap>(
				20);
		ImageCache imageCache = new ImageCache() {
			@Override
			public void putBitmap(String key, Bitmap value) {
				lruCache.put(key, value);
			}

			@Override
			public Bitmap getBitmap(String key) {
				return lruCache.get(key);
			}
		};
		ImageLoader imageLoader = new ImageLoader(requestQueue, imageCache);
		mNetworkImageView.setTag("url");
		mNetworkImageView.setImageUrl(imageUrl, imageLoader);
		Log.d("TAG", "yes2");
	}

	// 图片的 回调接口
	public interface VolleyImageCallback {
		void onSuccess(Bitmap bitmap, boolean isImmediate);
	}

	// 字符串的 回调接口
	public interface VolleyStringCallback {
		void onSuccess(String result);
	}
}