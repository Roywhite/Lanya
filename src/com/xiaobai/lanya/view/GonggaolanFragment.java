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
import com.xiaobai.lanya.util.ToastNoLooperUtil;

public class GonggaolanFragment extends Fragment {
	
	private TextView mHead;
	private ImageView mCamaro;
	private LanyaService mLanyaService;//蓝牙查找服务
	private Intent intent;
	private BlueToothControllerUtil mController = new BlueToothControllerUtil();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_gonggaolan_fragment, container, false);
		initView(view);
		initData();
		return view;
	}

	private void initData() {
		mHead.setText("公告栏");
		mCamaro.setImageResource(R.drawable.camaro_image);
		ToastNoLooperUtil.showToast(getContext(),"抓住这个程序猿小哥哥来祭天！！！          ヽ(●-`Д´-)ノ");
		//加载服务
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
		mHead = (TextView) view.findViewById(R.id.head_name_in_gonggao).findViewById(R.id.head_name);
		mCamaro = (ImageView) view.findViewById(R.id.head_name_in_gonggao).findViewById(R.id.head_camaro);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if(intent!=null) {
			getActivity().stopService(intent);
		}
	}
}
