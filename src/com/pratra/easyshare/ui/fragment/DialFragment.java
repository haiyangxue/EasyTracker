package com.pratra.easyshare.ui.fragment;

import com.pratra.easyshare.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

/**
 * 短信Fragment的界面
 * 
 * @author fankaichao
 */
public class DialFragment extends Fragment {
	
	private Button 
	button0,button1,button2,button3,button4,button5,
	button6,button7,button8,button9,button10,button11;
	private ImageButton deleteButton,clearButton,dialButton;
	
	private EditText numText;
	private String number;
	private View view;
	
	private Button btn;
	private boolean dial_state=false;
	private View dialBoard;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_dial,container,false);
		
		numText = (EditText) view.findViewById(R.id.mobile);
		numText.setInputType(InputType.TYPE_NULL); 
		
		this.initializeNumButtons();
		this.initializeNumButtonsClickListener();
		
		//调用到clearButton按钮
		clearButton = (ImageButton) view.findViewById(R.id.clear_button);
		//为dialButton按钮设置监听器，监听器类型是在本视图的监听器   
		clearButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				number = ""+numText.getText();
				if(number.length()>0){
					numText.setText("");
				}
			}
		});
		
		//调用到dialButton按钮
		deleteButton = (ImageButton) view.findViewById(R.id.delete_button);
		//为dialButton按钮设置监听器，监听器类型是在本视图的监听器   
		deleteButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				number = ""+numText.getText();
				if(number.length()>0){
					number=number.substring(0, number.length()-1);
					numText.setText(number);
				}
			}
		});
		
		//调用到dialButton按钮   
        dialButton = (ImageButton) view.findViewById(R.id.dial_button);  
        //为dialButton按钮设置监听器，监听器类型是在本视图的监听器
        dialButton.setOnClickListener(new View.OnClickListener() {  
              
            public void onClick(View view) {
            	if(numText.getText().length()>0){
            		//新建一个intent对象，进行调用系统的打电话的方法，然后传递号码过去   
                    Intent intent = new Intent(Intent.ACTION_CALL , Uri.parse("tel:" +  numText.getText()));  
                    //相应事件   
                    getActivity().startActivity(intent);
            	}
            	else{
            		Toast.makeText(getActivity(), "请输入手机号", Toast.LENGTH_SHORT).show();
            	}
            }  
        });
		
        btn = (Button) view.findViewById(R.id.button);
        btn.setBackgroundResource(R.drawable.btn_pressed);
        dialBoard = (View) view.findViewById(R.id.dialBoard);
		btn.setOnClickListener(new View.OnClickListener(){
			
			public void onClick(View V){
				if(dial_state){
					btn.setBackgroundResource(R.drawable.btn_pressed);
					dialBoard.setVisibility(View.VISIBLE);
					dial_state = false;
				}
				else{
					btn.setBackgroundResource(R.drawable.btn_default);
					dialBoard.setVisibility(View.GONE);
					dial_state = true;
				}
			}
		});
        
		return view;
	}
	
	private void initializeNumButtons(){
		button0 = (Button) view.findViewById(R.id.button0);
		button1 = (Button) view.findViewById(R.id.button1);
		button2 = (Button) view.findViewById(R.id.button2);
		button3 = (Button) view.findViewById(R.id.button3);
		button4 = (Button) view.findViewById(R.id.button4);
		button5 = (Button) view.findViewById(R.id.button5);
		button6 = (Button) view.findViewById(R.id.button6);
		button7 = (Button) view.findViewById(R.id.button7);
		button8 = (Button) view.findViewById(R.id.button8);
		button9 = (Button) view.findViewById(R.id.button9);
		button10 = (Button) view.findViewById(R.id.button10);//*
		button11 = (Button) view.findViewById(R.id.button11);//#
	}

	private void initializeNumButtonsClickListener(){
		button0.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s0 = (String)button0.getText();
				number = numText.getText()+s0;
				numText.setText(number);
//				Log.d("button0>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button0.getText());
			}
		});
		button1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s1 = (String)button1.getText();
				number = numText.getText()+s1;
				numText.setText(number);
//				Log.d("button1>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button1.getText());
			}
		});
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s2 = (String)button2.getText();
				number = numText.getText()+s2;
				numText.setText(number);
//				Log.d("button2>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button2.getText());
			}
		});
		button3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s3 = (String)button3.getText();
				number = numText.getText()+s3;
				numText.setText(number);
//				Log.d("button3>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button3.getText());
			}
		});
		button4.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s4 = (String)button4.getText();
				number = numText.getText()+s4;
				numText.setText(number);
//				Log.d("button4>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button4.getText());
			}
		});
		button5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s5 = (String)button5.getText();
				number = numText.getText()+s5;
				numText.setText(number);
//				Log.d("button5>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button5.getText());
			}
		});
		button6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s6 = (String)button6.getText();
				number = numText.getText()+s6;
				numText.setText(number);
//				Log.d("button6>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button6.getText());
			}
		});
		button7.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s7 = (String)button7.getText();
				number = numText.getText()+s7;
				numText.setText(number);
//				Log.d("button7>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button7.getText());
			}
		});
		button8.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s8 = (String)button8.getText();
				number = numText.getText()+s8;
				numText.setText(number);
//				Log.d("button8>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button8.getText());
			}
		});
		button9.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s9 = (String)button9.getText();
				number = numText.getText()+s9;
				numText.setText(number);
//				Log.d("button9>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button9.getText());
			}
		});
		button10.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s10 = (String)button10.getText();
				number = numText.getText()+s10;
				numText.setText(number);
//				Log.d("button10>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button10.getText());
			}
		});
		button11.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				String s11 = (String)button11.getText();
				number = numText.getText()+s11;
				numText.setText(number);
//				Log.d("button11>>>>>>>>>>>>>>>>>>>>>>>>>>>>>", (String)button11.getText());
			}
		});
	}
}


//package com.pratra.easyshare.ui.fragment;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.util.DisplayMetrics;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//import android.widget.FrameLayout.LayoutParams;
//
///**
// * 短信Fragment的界面
// * 
// * @author fankaichao
// */
//public class DialFragment extends Fragment {
//	@Override
//	public View onCreateView(LayoutInflater inflater, ViewGroup container,
//			Bundle savedInstanceState) {
//		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT,
//				LayoutParams.MATCH_PARENT);
//		FrameLayout fl = new FrameLayout(getActivity());
//		fl.setLayoutParams(params);
//		DisplayMetrics dm = getResources().getDisplayMetrics();
//		final int margin = (int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_DIP, 8, dm);
//		TextView v = new TextView(getActivity());
//		params.setMargins(margin, margin, margin, margin);
//		v.setLayoutParams(params);
//		v.setLayoutParams(params);
//		v.setGravity(Gravity.CENTER);
//		v.setText("拨号界面");
//		v.setTextSize((int) TypedValue.applyDimension(
//				TypedValue.COMPLEX_UNIT_SP, 24, dm));
//		fl.addView(v);
//		return fl;
//	}
//}