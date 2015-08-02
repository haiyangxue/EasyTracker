package com.pratra.easyshare.ui.fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.pratra.easyshare.R;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

/**
 * 短信Fragment的界面
 * 
 * @author fankaichao
 */
public class MessageFragment extends Fragment {
	
	private View view;
	private Button sendButton,btn,haveSend,haveRecieved;
	private EditText mEditText1;
	private EditText mEditText2;
	private ListView haveSendMessageList;
	private ListView haveRecievedMessageList;
	private View messageListLayout;
	private boolean list_state = false;
	private static int SENDPAGE=1;
	private static int RECIEVEPAGE=2;
	private int MessageBoxState=-1;
	
	private Uri SMS_INBOX = Uri.parse("content://sms/");
	private ArrayList<HashMap<String, String>> hRecievedListItem;
	private ArrayList<HashMap<String, String>> hSendListItem;
	private SimpleAdapter hsmSimpleAdapter;
	private SimpleAdapter hrmSimpleAdapter;
	private MyMessageAdapter myMesSendAdapter;
	private MyMessageAdapter myMesRecieveAdapter;
	private SmsObserver smsObserver;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_message,container,false);
		
		/* 透过findViewById建构子来建构EditText1,EditText2与Button对象 */
		mEditText1 = (EditText) view.findViewById(R.id.numEditText);
		mEditText2 = (EditText) view.findViewById(R.id.mesEditText);
		sendButton = (Button) view.findViewById(R.id.sendMesButton);
		messageListLayout = (View) view.findViewById(R.id.messagelistlayout);
		haveSendMessageList = (ListView) view.findViewById(R.id.have_send_messagelist);
		haveRecievedMessageList = (ListView) view.findViewById(R.id.have_recieved_messagelist);
		haveSend = (Button) view.findViewById(R.id.have_send);
		haveRecieved = (Button) view.findViewById(R.id.have_recieved);
		
		
		/* 设定onClickListener 让使用者点选EditText时做出反应 */
		mEditText1.setOnClickListener(new EditText.OnClickListener() {
			public void onClick(View v) {
				/* 点选EditText时清空内文 */
				mEditText1.setText("");
			}
		});
		
		/* 设定onClickListener 让使用者点选EditText时做出反应 */
		mEditText2.setOnClickListener(new EditText.OnClickListener() {
			public void onClick(View v) {
				/* 点选EditText时清空内文 */
				mEditText2.setText("");
			}
		});
		
		/* 设定onClickListener 让使用者点选Button时做出反应 */
		sendButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				/* 由EditText1取得简讯收件人电话 */
				String strDestAddress = mEditText1.getText().toString();
				/* 由EditText2取得简讯文字内容 */
				String strMessage = mEditText2.getText().toString();
				/* 建构一取得default instance的 SmsManager对象 */
				SmsManager smsManager = SmsManager.getDefault();

				/* 检查收件人电话格式与简讯字数是否超过70字符 */
				if(isPhoneNumberValid(strDestAddress)==true){
					try {
						/*
						 * 两个条件都检查通过的情况下,发送简讯 *
						 * 先建构一PendingIntent对象并使用getBroadcast()方法进行Broadcast *
						 * 将PendingIntent,电话,简讯文字等参数传入sendTextMessage()方法发送简讯
						 */
						PendingIntent mPI = PendingIntent.getBroadcast(
								getActivity(), 0, new Intent(), 0);
						smsManager.sendTextMessage(strDestAddress, null,
								strMessage, mPI, null);
						
						Toast.makeText(getActivity(), "正在发送...", Toast.LENGTH_SHORT).show();
						mEditText1.setText("");
						mEditText2.setText("");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});

		/*短信列表的初始化设置*/
		MessageBoxState = SENDPAGE;
		haveSendMessageList.setVisibility(View.VISIBLE);
		haveRecievedMessageList.setVisibility(View.GONE);
		haveSend.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_on);
		haveRecieved.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_off);
		haveSend.setTextColor(Color.parseColor("#FFFFFF"));
		haveRecieved.setTextColor(Color.parseColor("#40AA53"));
		
		/* 设定onClickListener 让使用者点选Button时做出反应 */
		haveSend.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if(MessageBoxState!=SENDPAGE){
					MessageBoxState=SENDPAGE;
					haveSendMessageList.setVisibility(View.VISIBLE);
					haveRecievedMessageList.setVisibility(View.GONE);
					haveSend.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_on);
					haveRecieved.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_off);
					haveSend.setTextColor(Color.parseColor("#FFFFFF"));
					haveRecieved.setTextColor(Color.parseColor("#40AA53"));
				}
				else{
					Toast.makeText(getActivity(), "您已在发送箱", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		/* 设定onClickListener 让使用者点选Button时做出反应 */
		haveRecieved.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				if(MessageBoxState!=RECIEVEPAGE){
					MessageBoxState=RECIEVEPAGE;
					haveRecievedMessageList.setVisibility(View.VISIBLE);
					haveSendMessageList.setVisibility(View.GONE);
					haveSend.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_off);
					haveRecieved.setBackgroundResource(R.drawable.bg_have_send_recieve_btn_on);
					haveSend.setTextColor(Color.parseColor("#40AA53"));
					haveRecieved.setTextColor(Color.parseColor("#FFFFFF"));
				}
				else{
					Toast.makeText(getActivity(), "您已在收件箱", Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		/*短信列表，收起和展开的按钮*/
		btn = (Button) view.findViewById(R.id.button);
        btn.setBackgroundResource(R.drawable.btn_pressed);
        mEditText2.setVisibility(View.GONE);
		messageListLayout.setVisibility(View.VISIBLE);
		btn.setOnClickListener(new View.OnClickListener(){
			public void onClick(View V){
				if(list_state){
					btn.setBackgroundResource(R.drawable.btn_pressed);
					mEditText2.setVisibility(View.GONE);
					messageListLayout.setVisibility(View.VISIBLE);
					list_state = false;
				}
				else{
					btn.setBackgroundResource(R.drawable.btn_default2);
					mEditText2.setVisibility(View.VISIBLE);
					messageListLayout.setVisibility(View.GONE);
					list_state = true;
				}
			}
		});
		
		hSendListItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
		hRecievedListItem = new ArrayList<HashMap<String, String>>();/*在数组中存放数据*/
		/*获取短信内容*/
		this.getSmsFromPhone();

		myMesSendAdapter = new MyMessageAdapter(view.getContext(), hSendListItem);
		haveSendMessageList.setAdapter(myMesSendAdapter);//为ListView绑定适配器
		myMesRecieveAdapter = new MyMessageAdapter(view.getContext(), hRecievedListItem);
		haveRecievedMessageList.setAdapter(myMesRecieveAdapter);//为ListView绑定适配器
		
		smsObserver = new SmsObserver(getActivity(), smsHandler);  
		getActivity().getContentResolver().registerContentObserver(SMS_INBOX, true, smsObserver);  
		
		return view;
	}

	/*验证号码是否符合规范*/
	public boolean isPhoneNumberValid(String phoneNumber){
		if(phoneNumber.length()==11){
			for(int i=0; i<phoneNumber.length(); i++){
				String num=phoneNumber.substring(i, i+1);
				if(!isInt(num)){
					Toast.makeText(getActivity(), "号码内存在非数字字符", Toast.LENGTH_SHORT).show();
					return false;
				}
			}
			return true;
		}
		else
			Toast.makeText(getActivity(), "号码长度不符合规定", Toast.LENGTH_SHORT).show();
			return false;
	}
	
	/*验证字符是否是数字*/
	public boolean isInt(String num){
		if(num.equals("0")
				||num.equals("1")
				||num.equals("2")
				||num.equals("3")
				||num.equals("4")
				||num.equals("5")
				||num.equals("6")
				||num.equals("7")
				||num.equals("8")
				||num.equals("9")){
			return true;
		}
		return false;
	}

	/*从本机上获取相关的短信内容*/
    public void getSmsFromPhone() {
    	
    	hRecievedListItem.clear();
    	hSendListItem.clear();
    	
        ContentResolver cr = getActivity().getContentResolver();  
        String[] projection = new String[] { "address", "person", "date", "body", "type", "read" };//"_id", 
//        String where = " address = '1066321332' AND date > " + (System.currentTimeMillis() - 10 * 60 * 1000);  
        Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");//date desc  
        if (null == cur)
        	return;  
        while (cur.moveToNext()) {
        	
        	String number = cur.getString(cur.getColumnIndex("address"));//手机号
        	String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
        	long date = cur.getInt(cur.getColumnIndex("date"));
        	String body = cur.getString(cur.getColumnIndex("body"));
        	int type = cur.getInt(cur.getColumnIndex("type"));
        	int read = cur.getInt(cur.getColumnIndex("read"));
        	
        	Date dt = new Date(date);//时间  
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
            String receiveTime = format.format(dt);  
        	
        	/*逐条将信息以HashMap的形式添加到ListView需要绑定的数据ArrayList中*/
    		HashMap<String, String> map = new HashMap<String, String>();
			map.put("MessageItemAdress", number);
			if(name == null){
				map.put("MessageItemPerson", "陌生人");
			}
			else{
				map.put("MessageItemPerson", name);
			}
			map.put("MessageItemDate", receiveTime);
			map.put("MessageItemBody", body);
			if(type==1){
				if(read==0){
					map.put("ItemImage", ""+R.drawable.ic_mes_not_read);//加入图片
				}
				else if(read==1){
					map.put("ItemImage", ""+R.drawable.ic_mes_have_read);//加入图片
				}
			}
			else if(type==2){
				map.put("ItemImage", ""+R.drawable.ic_mes_send);//加入图片
			}
			
			if(type==1){
				hRecievedListItem.add(map);
			}
			else if(type==2){
				hSendListItem.add(map);
			}
			
            //这里我是要获取自己短信服务号码中的验证码~~
//        	Pattern pattern = Pattern.compile(" [a-zA-Z0-9]{10}");
//        	Matcher matcher = pattern.matcher(body);
//        	if (matcher.find()) {
//        		String res = matcher.group().substring(1, 11);
//        		mobileText.setText(res);  
//            }  
        }  
    }
	
    public Handler smsHandler = new Handler() {
    	
        //这里可以进行回调的操作  
    	public void handleMessage(Message msg) {
            switch (msg.what) {  
            case 1:  
//            	ArrayList<HashMap<String, String>> outbox = (ArrayList<HashMap<String, String>>) msg.obj;
            	((BaseAdapter) myMesRecieveAdapter).notifyDataSetChanged();
                break;  
            default:  
                break;
            }  
        }  
    };
    
    class SmsObserver extends ContentObserver {
    	
    	private Handler smsHandler;
    	
    	public SmsObserver(Context context, Handler handler) {
    		super(handler);
    		smsHandler = handler;
    	}
  
        @Override  
        public void onChange(boolean selfChange) {
        	super.onChange(selfChange);  
            //每当有新短信到来时，使用我们获取短消息的方法
            getSmsFromPhone();
            smsHandler.obtainMessage(1, hRecievedListItem).sendToTarget();
        }  
    }  
    
    public class MyMessageAdapter extends BaseAdapter {
    	
    	private ArrayList<HashMap<String, String>> listItem;
    	private LayoutInflater inflater;

        public MyMessageAdapter(Context context, ArrayList<HashMap<String, String>> listItem){  
            this.listItem = listItem;
            this.inflater = (LayoutInflater) context
    				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
      
        @Override  
        public int getCount() {  
            return (listItem==null)?(0):listItem.size();  
        }  
      
        @Override  
        public Object getItem(int position) {  
            return listItem.get(position);  
        }  
      
        @Override  
        public long getItemId(int position) {  
            return position;  
        }
        
        public class ViewHolder{
        	TextView MessageItemAdress;
        	TextView MessageItemPerson;
        	TextView MessageItemDate;
        	ImageView ItemImage;
        	TextView MessageItemBody;
        }
      
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
        	HashMap<String, String> message = (HashMap<String, String>) getItem(position);
            ViewHolder viewHolder = null;  
            if(convertView==null){
            	
                Log.d("MyBaseAdapter", "新建convertView,position="+position);
                
                convertView = inflater.inflate(R.layout.fragment_message_list_item, null);  

                viewHolder = new ViewHolder();
                viewHolder.MessageItemAdress = (TextView)convertView.findViewById(R.id.MessageItemAdress);  
                viewHolder.MessageItemPerson = (TextView)convertView.findViewById(R.id.MessageItemPerson);
                viewHolder.MessageItemDate = (TextView)convertView.findViewById(R.id.MessageItemDate);
                viewHolder.ItemImage = (ImageView)convertView.findViewById(R.id.ItemImage);
                viewHolder.MessageItemBody = (TextView)convertView.findViewById(R.id.MessageItemBody);
                  
                convertView.setTag(viewHolder);  
            }else{  
                viewHolder = (ViewHolder)convertView.getTag();  
                Log.d("MyBaseAdapter", "旧的convertView,position="+position);  
            }  

            viewHolder.MessageItemAdress.setText(message.get("MessageItemAdress"));
            viewHolder.MessageItemPerson.setText(message.get("MessageItemPerson"));
            viewHolder.MessageItemDate.setText(message.get("MessageItemDate"));
            viewHolder.ItemImage.setImageResource(Integer.parseInt(message.get("ItemImage")));
            viewHolder.MessageItemBody.setText(message.get("MessageItemBody"));

            return convertView;  
        }  
      
    }

}