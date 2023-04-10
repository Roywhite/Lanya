package com.xiaobai.lanya.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.service.LanyaService;
import com.xiaobai.lanya.util.BlueToothControllerUtil;
import com.xiaobai.lanya.util.MediaPlay;
import com.xiaobai.lanya.util.ToastNoLooperUtil;

public class SetFragment extends Fragment {
	private BlueToothControllerUtil mController = new BlueToothControllerUtil();
	private TextView mHead;
	private TextView mTestMedia;//测试音效
	private TextView mDark;//夜间模式
	private TextView mSun;//日间模式
	private ImageView mCamaro;
	private LanyaService mLanyaService;//蓝牙查找服务
	private Intent intent;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_set_fragment, container, false);
		initView(view);
		initData();
		initListener();
		return view;
	}

	private void initListener() {
		mTestMedia.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MediaPlay mediaPlay = new MediaPlay(v.getContext());
				mediaPlay.start(v.getContext());
			}
		});
		mDark.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(getContext(),"程序猿小哥哥正在加急开发哟！！");
			}
		});
		mSun.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(getContext(),"你现在不就在日间模式嘛？大傻瓜");
			}
		});
	}

	private void initData() {
		mHead.setText("设置");
		mCamaro.setImageResource(R.drawable.camaro_image);

		mLanyaService = new LanyaService(getContext());
		boolean supportBlueTooth = mController.isSupportBlueTooth();
		if(supportBlueTooth==true) {
			if (mController.getBlueToothStatus() == true) {
				intent = new Intent(getContext(), LanyaService.class);
				getActivity().startService(intent);
			}
		}
	}

	private void initView(View view) {
		
		mHead = (TextView) view.findViewById(R.id.head_name_in_set).findViewById(R.id.head_name);
		mCamaro = (ImageView) view.findViewById(R.id.head_name_in_set).findViewById(R.id.head_camaro);
		mTestMedia = (TextView) view.findViewById(R.id.set_dark);
		mDark = (TextView) view.findViewById(R.id.set_dark1);
		mSun = (TextView) view.findViewById(R.id.set_dark2);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(intent!=null) {
			getActivity().stopService(intent);
		}
	}
}