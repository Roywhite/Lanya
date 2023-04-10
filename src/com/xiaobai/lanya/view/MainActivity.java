package com.xiaobai.lanya.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.support.v4.widget.ViewDragHelper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.xiaobai.lanya.R;
import com.xiaobai.lanya.adapter.DeviceAdapter;
import com.xiaobai.lanya.service.LanyaService;
import com.xiaobai.lanya.util.BlueToothControllerUtil;
import com.xiaobai.lanya.util.MediaPlay;
import com.xiaobai.lanya.util.ToastNoLooperUtil;

import java.lang.reflect.Field;

public class MainActivity extends FragmentActivity {
	// 定义FragmentTabHost对象
	private BlueToothControllerUtil mController = new BlueToothControllerUtil();;//蓝牙工具类
	private FragmentTabHost mTabHost;
	private DrawerLayout mDrawer;//整个左侧菜单
	private LinearLayout mLinear;//主界面
	private TextView mPersonMessage;//个人信息
	private TextView mSendBack;//反馈
	private TextView mSetLanya;//系统蓝牙设置界面
	private TextView mHelp;//自助服务
	private TextView mVersion;//版本号
	private TextView mLogOut;//退出登录
	private LanyaService mLanyaService;//蓝牙查找服务
	private Intent intent;
	private SharedPreferences sp;
	private DeviceAdapter mAdapter;//设备布局适配器

	// 定义一个布局
	private LayoutInflater layoutInflater;

	// 定义数组来存放Fragment界面
	private Class fragmentArray[] = { LanyaFragment.class, GonggaolanFragment.class, SetFragment.class };

	// 定义数组来存放导航图标
	private int mImageViewArray[] = { R.drawable.one_change_icon_image, R.drawable.two_change_icon_image,
			R.drawable.three_change_icon_image };

	// Tab选项卡的文字
	private String mTextviewArray[] = { "设备", "公告栏", "设置" };

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initView();
		initDate();

		//注册广播,Fragment中需要使用getActivity()获取到Activity对象
		IntentFilter filter = new IntentFilter();
		//查找设备
		filter.addAction(BluetoothDevice.ACTION_FOUND);

		registerReceiver(receivers, filter);

		initListener();
	}

	private void initDate() {
		mLanyaService = new LanyaService(MainActivity.this);
        sp =  this.getSharedPreferences("lanya_device", Context.MODE_MULTI_PROCESS);
		boolean supportBlueTooth = mController.isSupportBlueTooth();
        if(supportBlueTooth==true) {
			if (mController.getBlueToothStatus() == true) {
				intent = new Intent(this, LanyaService.class);
				startService(intent);
			}
		}
	}

	//定义一个广播
	private BroadcastReceiver receivers = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
					String rssi = sp.getString(remoteDevice.getAddress(),null);
					MediaPlay mediaPlay = new MediaPlay(context);
					if(rssi!=null&&Double.parseDouble(rssi)>8){
						ToastNoLooperUtil.showToast(MainActivity.this,remoteDevice.getName()+"连接丢失!!!");
						//播放系统提示音
						mediaPlay.start(context);
					}else if(rssi==null){
						//预留部分
						//ToastNoLooperUtil.showToast(MainActivity.this,remoteDevice.getName()+"连接丢失!!!");
					}else if(rssi!=null&&Double.parseDouble(rssi)<=8){
						ToastNoLooperUtil.showToast(MainActivity.this,remoteDevice.getName()+"距离："+rssi);
					}
				}
		}
	};

	//设置监听
	private void initListener() {
		mDrawer.setDrawerListener(new DrawerListener() {
			
			@Override
			public void onDrawerStateChanged(int arg0) {
			}
			
			@Override
			public void onDrawerSlide(View drawView, float slideOffset) {
				//设置主布局随菜单滑动而滑动
				int drawViewWidth = drawView.getWidth();
				mLinear.setTranslationX(drawViewWidth*slideOffset);
			}
			
			@Override
			public void onDrawerOpened(View arg0) {
			}
			
			@Override
			public void onDrawerClosed(View arg0) {
			}
		});

		mPersonMessage.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"修改个人信息");
			}
		});
		mSendBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"反馈");
			}
		});
		mVersion.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"Version:3.1.0");
			}
		});
		mLogOut.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"退出登录");
			}
		});
		mHelp.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ToastNoLooperUtil.showToast(MainActivity.this,"自助服务");
			}
		});
		mSetLanya.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mController.isSupportBlueTooth() == false)
					ToastNoLooperUtil.showToast(MainActivity.this, "该设备不支持蓝牙功能");
				else {
					if (mController.getBlueToothStatus() == false) {
						new AlertDialog.Builder(MainActivity.this).setTitle("提示")
								.setMessage("请打开蓝牙，并把蓝牙可见设为永不超时")
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog, int which) {
										Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
										startActivity(intent);
									}
								}).show();
					} else if (mController.getBlueToothStatus() == true) {
						Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
						startActivity(intent);
					}
				}
			}
		});
	}

	/**
	 * 初始化组件
	 */
	private void initView() {
		// 实例化布局对象
		layoutInflater = LayoutInflater.from(this);
		mDrawer = (DrawerLayout) findViewById(R.id.drawer_main);
		setDrawerLeftEdgeSize(this, mDrawer, 0.6f);
		mLinear = (LinearLayout) findViewById(R.id.main_main);

		mPersonMessage = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_person_message);
		mSendBack = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_sendBack);
		mVersion = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_version);
		mLogOut = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_logout);
		mHelp = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_Help);
		mSetLanya = (TextView) findViewById(R.id.youhua).findViewById(R.id.youhua_setLanya);


		// 实例化TabHost对象，得到TabHost
		mTabHost = (FragmentTabHost) findViewById(R.id.tabhost);
		mTabHost.setup(this, getSupportFragmentManager(), R.id.qq_tabcontent);
		//去除底部导航栏分割线
		mTabHost.getTabWidget().setDividerDrawable(null);

		// 得到fragment的个数
		int count = fragmentArray.length;

		for (int i = 0; i < count; i++) {
			// 为每一个Tab按钮设置图标、文字和内容
			TabSpec tabSpec = mTabHost.newTabSpec(mTextviewArray[i]).setIndicator(getTabItemView(i));
			// 将Tab按钮添加进Tab选项卡中
			mTabHost.addTab(tabSpec, fragmentArray[i], null);
		}
	}

	/**
	 * 给Tab按钮设置图标和文字
	 */
	private View getTabItemView(int index) {
		View view = layoutInflater.inflate(R.layout.nav_item, null);

		ImageView imageView = (ImageView) view.findViewById(R.id.nav_icon_iv);
		imageView.setImageResource(mImageViewArray[index]);

		TextView textView = (TextView) view.findViewById(R.id.nav_text_tv);
		textView.setText(mTextviewArray[index]);

		return view;
	}
	
	//点击返回键返回桌面而不是退出程序
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
 
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            home.addCategory(Intent.CATEGORY_HOME);
            startActivity(home);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

	/**
	 * 抽屉滑动范围控制
	 *
	 * @param activity
	 * @param drawerLayout
	 * @param displayWidthPercentage 占全屏的份额0~1
	 */
	@SuppressLint("LongLogTag")
	private void setDrawerLeftEdgeSize(Activity activity, DrawerLayout drawerLayout, float displayWidthPercentage) {
		if (activity == null || drawerLayout == null)
			return;
		try {
			// find ViewDragHelper and set it accessible
			Field leftDraggerField = drawerLayout.getClass().getDeclaredField("mLeftDragger");
			leftDraggerField.setAccessible(true);
			ViewDragHelper leftDragger = (ViewDragHelper) leftDraggerField.get(drawerLayout);
			// find edgesize and set is accessible
			Field edgeSizeField = leftDragger.getClass().getDeclaredField("mEdgeSize");
			edgeSizeField.setAccessible(true);
			int edgeSize = edgeSizeField.getInt(leftDragger);
			// set new edgesize
			// Point displaySize = new Point();
			DisplayMetrics dm = new DisplayMetrics();
			activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
			edgeSizeField.setInt(leftDragger, Math.max(edgeSize, (int) (dm.widthPixels * displayWidthPercentage)));
		} catch (NoSuchFieldException e) {
			Log.e("NoSuchFieldException", e.getMessage().toString());
		} catch (IllegalArgumentException e) {
			Log.e("IllegalArgumentException", e.getMessage().toString());
		} catch (IllegalAccessException e) {
			Log.e("IllegalAccessException", e.getMessage().toString());
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(intent!=null) {
			stopService(intent);
		}
		if(receivers!=null) {
			unregisterReceiver(receivers);
		}
	}
}