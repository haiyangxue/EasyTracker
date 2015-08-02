package com.pratra.easyshare.httpapi;

public class Urls {
	
	public final static int DISTANCE = 50;
	
	
	public final static String HOST = "211.87.229.60";// 211.87.229.64   211.87.229.30
														// 121.250.220.182
	public static final String PHOTO_BASIC_URL = "http://" + HOST
			+ ":8080/EasyTracker/upServer?";
	public static final String LOC_BASIC_URL = "http://" + HOST
			+ ":8080/EasyTracker/locationServer?";

	// 请求完成标示
	public static final String REQUEST_REQUEST_END = "REQUEST_END";

	// 请求客户端拍照
	public static final String TAKINGPHOTO = PHOTO_BASIC_URL
			+ "userid=HOST&request=ASKING_PHOTO";
	// 请求客户端发送经纬度
	public static final String SENDING_LOCATION = LOC_BASIC_URL
			+ "userid=HOST&request=ASKING_LOCATION";
	// 请求完成
	public static final String REQUESTEND = PHOTO_BASIC_URL
			+ "userid=HOST&request=REQUEST_END";
	// 图片地址
	public static final String PHOTO_URL ="http://"+HOST+":8080/EasyTracker/files/photo.jpg";

}
