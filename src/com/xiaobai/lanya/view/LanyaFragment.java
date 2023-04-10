package com.xiaobai.lanya.view;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.adapter.DeviceAdapter;
import com.xiaobai.lanya.util.BlueToothControllerUtil;
import com.xiaobai.lanya.util.RssiUtil;
import com.xiaobai.lanya.util.ToastNoLooperUtil;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * 功能：
 * 1.进入该界面，获取sp文件列表中的绑定了的设备，然后根据存储的name：address获取他们的连接状态，如果断开，报警
 * 2.该界面显示绑定的设备列表，丢失的设备显示红色，连接的设备显示黑色
 * 3.要解决的问题：（1）写一个服务，后台获取连接状态与绑定的设备进行比较
 * （2）打开页面加载sp列表
 * （3）获取name和address错误的问题
 */


//Fragment不能通过在按钮上加onclick方法设置监听
public class LanyaFragment extends Fragment {
	private BlueToothControllerUtil mController = new BlueToothControllerUtil();
	private TextView mHead;
	private TextView mText;//文字
	private Switch mSwitch;//开关
	private ListView mListView;//显示设备
	private DeviceAdapter mAdapter;//设备布局适配器
	private ImageView mCamaro;
	private Context mContext;//定义上下文
	private Handler handler_open;//打开蓝牙的线程
	private List<BluetoothDevice> mDeviceList = new ArrayList<>();//查找到的设备
	private List<BluetoothDevice> mConnectDeviceList = new ArrayList<>();//连接到的设备
	private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();//绑定过的设备
	private List<BluetoothDevice> mBoundAndConDeviceList = new ArrayList<>();//绑定过的设备+连接到的设备
	private View view;//缓存Fragment view
	private SharedPreferences sp;// 保存数据:键值对
	private String rssi = null;
	private boolean mReceiverTag = false;   //广播接受者标识

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(view==null){
			view = inflater.inflate(R.layout.activity_lanya_fragment, container, false);
		}
		//缓存的view需要判断是否已经被加过parent， 如果有parent需要从parent删除，要不然会发生这个view已经有parent的错误。
		ViewGroup parent = (ViewGroup) view.getParent();
		//获取定位权限
		//bluetoothPermissions();
		if (parent != null) {
			parent.removeView(view);
		}
		if (!mReceiverTag) {
			//注册广播,Fragment中需要使用getActivity()获取到Activity对象
			IntentFilter filter = new IntentFilter();
			mReceiverTag = true;
			//查找设备
			filter.addAction(BluetoothDevice.ACTION_FOUND);
			getActivity().registerReceiver(receiver, filter);
		}
		//获取上下文
		this.mContext = getActivity();
		//初始化组件
		initView(view);
		//初始化组件数据
		initData();
		//设置监听
		initListner(getActivity());
		return view;
	}



    //定义一个广播
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String action = intent.getAction();
			BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				for(int i=0;i<mAdapter.getCount();i++){
					BluetoothDevice item = (BluetoothDevice)mAdapter.getItem(i);
					if(remoteDevice.getAddress().equals(item.getAddress())){
						int rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);//获取额外rssi值
						String distance = RssiUtil.getDistance(rssi);
						ToastNoLooperUtil.showToast(mContext,remoteDevice.getName()+" 距离:"+distance+"m");
						//mAdapter.setRssi(remoteDevice.getAddress(),distance);
						sp.edit().putString(remoteDevice.getAddress(),distance).commit();
						//mAdapter.setName((BluetoothDevice) mAdapter.getItem(i),"已绑定"+" 距离:"+distance+"m");
						mAdapter.refresh(mBondedDeviceList);
						break;
					}
				}

			}
        }
    };


	// 定义获取基于地理位置的动态权限
//	private void bluetoothPermissions() {
//		if (ContextCompat.checkSelfPermission(mContext, android.Manifest.permission.ACCESS_COARSE_LOCATION)
//				!= PackageManager.PERMISSION_GRANTED) {
//			ActivityCompat.requestPermissions(getActivity(), new String[]{
//					android.Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
//		}
//	}


	private void initListner(final Activity activity) {
		mSwitch.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final Switch sw = (Switch)v;
				boolean isChecked = sw.isChecked();
				if(isChecked){
					if(mController.isSupportBlueTooth()==false){
						ToastNoLooperUtil.showToast(mContext,"该设备不支持蓝牙...");
						sw.setChecked(false);
					}else {
						if (mController.getBlueToothStatus() == true) {
							ToastNoLooperUtil.showToast(mContext, "刷新设备中...");

							newHandler();
							//把滑块关掉
							new Handler().postDelayed(new Runnable() {
								@Override
								public void run() {
									//do something
									sw.setChecked(false);
								}
							}, 1000);
						} else if (mController.getBlueToothStatus() == false) {
							mBondedDeviceList.clear();
							mAdapter.refresh(mBondedDeviceList);
							ToastNoLooperUtil.showToast(mContext, "请在侧滑菜单‘连接蓝牙设备’中打开后刷新列表...");
							sw.setChecked(false);
						}
					}
				}
			}
		});
	}

	/**
	 * 打开蓝牙式的功能
	 * @param activity
	 */
	public void open(final Activity activity){
		//将自己的设备设为可被查找状态
		mController.enableVisibly(mContext);
		//查找设备
		mText.setText("正在查找设备");
		mController.turnOnBlueTooth(activity,0);
		mAdapter.refresh(mDeviceList);
		mController.findDevice();
		//ListView的监听事件：进行绑定
		//mListView.setOnItemClickListener(bindDeviceClick);
	}

	/**
	 * 关闭蓝牙的功能
	 */
	public void close(){

		mController.closeFindDevice();
		mText.setText("绑定设备历史");
		mController.turnOffBlueTooth();
		mBondedDeviceList = mController.getBondedDeviceList();
		mAdapter.refresh(mBondedDeviceList);
		mListView.setOnItemClickListener(null);

	}

	/**
	 * 在fragment中使用getSharedPreferences需要先获取上下文
	 */
	private void initData() {
		sp = this.getActivity().getSharedPreferences("lanya_device", Context.MODE_MULTI_PROCESS);
		mHead.setText("设备");
		//测试音频报警是否工作正常
//		MediaPlay mediaPlay = new MediaPlay(mContext);
//		mediaPlay.start(mContext);
		//判断是否支持蓝牙
		boolean supportBlueTooth = mController.isSupportBlueTooth();
		if(supportBlueTooth==false) ToastNoLooperUtil.showToast(getActivity(),"该设备不支持蓝牙功能");
		else {
			//判断蓝牙状态，初始化滑动开关
			boolean blueToothStatus = mController.getBlueToothStatus();
			if (blueToothStatus == false) {
				ToastNoLooperUtil.showToast(mContext,"请在侧滑菜单‘连接蓝牙设备’中打开相关设置！");
			} else{
				//获取绑定的设备
				mBondedDeviceList = mController.getBondedDeviceList();
				//mBondedDeviceList=connect();
				mController.closeFindDevice();
				mController.findDevice();
				//刷新界面
				mAdapter.refresh(mBondedDeviceList);
				//存入sp，先清除（避免重复数据）
				for(BluetoothDevice device:mBondedDeviceList){
					rssi = sp.getString(device.getAddress(), null);
					sp.edit().putString(device.getAddress(), rssi).commit();
				}
			}
			newHandler();
		}

		mListView.setAdapter(mAdapter);
		//ListView的监听事件：进行绑定
		//mListView.setOnItemClickListener(bindDeviceClick);
	}


	/**
	 * 线程的初始化
	 * @param
	 */

	public void  newHandler(){

		if(handler_open ==null){
			handler_open = new Handler();
			handler_open.post(new Runnable() {
				@Override
				public void run() {
					mController.closeFindDevice();
					mBondedDeviceList.clear();
					//这里写刷新界面的方法
					mController.findDevice();
					mBondedDeviceList = mController.getBondedDeviceList();
					//更新sp状态
					sp.edit().clear().commit();
					for(BluetoothDevice device:mBondedDeviceList){
						rssi = sp.getString(device.getAddress(), null);
						sp.edit().putString(device.getAddress(), rssi).commit();
					}
					mAdapter.refresh(mBondedDeviceList);
				}
			});
		}else{
			handler_open.post(new Runnable() {
				@Override
				public void run() {
					mController.closeFindDevice();
					mBondedDeviceList.clear();
					//这里写刷新界面的方法
					mController.findDevice();
					mBondedDeviceList = mController.getBondedDeviceList();
					//更新sp状态
					sp.edit().clear().commit();
					for(BluetoothDevice device:mBondedDeviceList){
						rssi = sp.getString(device.getAddress(), null);
						sp.edit().putString(device.getAddress(), rssi).commit();
					}
					mAdapter.refresh(mBondedDeviceList);
				}
			});
		}

	}
	private void initView(View view) {

		mHead = (TextView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_name);
		mCamaro = (ImageView) view.findViewById(R.id.head_name_in_lanya).findViewById(R.id.head_camaro);
		mSwitch = (Switch) view.findViewById(R.id.turn_blue_tooth);
		mText = (TextView) view.findViewById(R.id.lanya_text);
		mListView = (ListView) view.findViewById(R.id.device_list);
		mAdapter = new DeviceAdapter(mDeviceList, mContext);

		//
		//View list_view = View.inflate(mContext, android.R.layout.simple_list_item_2, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			ToastNoLooperUtil.showToast(getActivity(),"打开成功");
		}else{
			ToastNoLooperUtil.showToast(getActivity(),"打开失败");
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//销毁广播
		if (mReceiverTag) {   //判断广播是否注册
			mReceiverTag = false;   //Tag值 赋值为false 表示该广播已被注销
			getActivity().unregisterReceiver(receiver);
		}

	}

	/**
	 * 绑定的方法
	 */
//	private AdapterView.OnItemClickListener bindDeviceClick = new AdapterView.OnItemClickListener() {
//		@Override
//		public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//			final BluetoothDevice device = mDeviceList.get(i);
//			if(device.getBondState()==device.BOND_NONE){
//				device.createBond();
//			}
//		}
//	};



    /**
     * 显示绑定中连接的设备
     */
//    public List<BluetoothDevice> connect(){
//        Class<BluetoothAdapter> bluetoothAdapterClass = BluetoothAdapter.class;//得到BluetoothAdapter的Class对象
//        //得到连接状态的方法
//        Method method;
//        {
//            try {
//                method = bluetoothAdapterClass.getDeclaredMethod("getConnectionState", (Class[]) null);
//                //打开权限
//                method.setAccessible(true);
//                int state = (int) method.invoke(mController, (Object[]) null);
//
//                if (state == BluetoothAdapter.STATE_CONNECTED) {
//                    Log.i("BLUETOOTH", "BluetoothAdapter.STATE_CONNECTED");
//                    mBondedDeviceList = mController.getBondedDeviceList();
//                    Log.i("BLUETOOTH", "devices:" + mBondedDeviceList.size());
//                    ToastUtil.showToast(mContext,String.valueOf(mBondedDeviceList.size()));
//                    for (BluetoothDevice device : mBondedDeviceList) {
//                        Method isConnectedMethod = BluetoothDevice.class.getDeclaredMethod("isConnected", (Class[]) null);
//                        method.setAccessible(true);
//                        boolean isConnected = (boolean) isConnectedMethod.invoke(device, (Object[]) null);
//                        if (isConnected) {
//                            Log.i("BLUETOOTH", "connected:" + device.getName());
//							ToastUtil.showToast(mContext,"test");
//                            mConnectDeviceList.add(device);//把连接上的设备存入集合中，以备后期使用
//                            mAdapter.setName(device,"已连接");
//                        }
//
//                    }
//                    mBoundAndConDeviceList.addAll(mBondedDeviceList);
//                    mBoundAndConDeviceList.addAll(mConnectDeviceList);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//		return mBoundAndConDeviceList;
//    }
}
