package com.xiaobai.lanya.util;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 蓝牙适配器工具类
 */
public class BlueToothControllerUtil {
    private BluetoothAdapter mAdapter;//本地蓝牙适配器
    private int rssi;//信号强度，用于计算距离

    /**
     * 获取蓝牙的rssi
     * @param rssi
     */
    public void setRssi(int rssi){
        this.rssi = rssi;
    }

    /**
     * 将rssi传出到外部
     * @return
     */
    public int getRssi(){
        return rssi;
    }

    public BlueToothControllerUtil(){
        mAdapter = BluetoothAdapter.getDefaultAdapter();
    }
    /**
     * 是否支持蓝牙
     * @return true 支持，false  不支持
     */
    public boolean isSupportBlueTooth(){

        if(mAdapter != null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 判断当前蓝牙状态
     * @return true 打开，false  关闭
     */
    public boolean getBlueToothStatus(){
        assert (mAdapter!=null);
        return mAdapter.isEnabled();
    }

    /**
     * 打开蓝牙
     * @param activity
     * @param requestCode
     */
    public void turnOnBlueTooth(Activity activity,int requestCode){
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent,requestCode);
        //mAdapter.enable();
    }

    /**
     * 开启蓝牙（无提示框）
     */
    public void getDevies(){
        mAdapter.enable();
    }

    /**
     * 关闭蓝牙
     */
    public void turnOffBlueTooth() {
        mAdapter.disable();
    }

    /**
     * 打开蓝牙可见性
     * @param context
     */
    public void enableVisibly(Context context){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        //setDiscoverableTimeout(600);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
        context.startActivity(discoverableIntent);
    }

    /**
     * 查找普通设备
     */
    public void findDevice(){
        assert (mAdapter!=null);
        mAdapter.startDiscovery();
    }

    //查找LE设备
    public void findLEDevice(){
         BluetoothAdapter.LeScanCallback	mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi,final byte[] scanRecord)
            {
                setRssi(rssi);
            }
        };
        assert (mAdapter!=null);
        mAdapter.startLeScan(mLeScanCallback);
    }
    /**
     * 关闭查找功能
     */
    public void closeFindDevice(){
        assert (mAdapter!=null);
        mAdapter.cancelDiscovery();
    }

    /**
     * 获取绑定设备
     * @return
     */
    public List<BluetoothDevice> getBondedDeviceList(){
        return new ArrayList<>(mAdapter.getBondedDevices());
    }

    /**
     * 判断蓝牙是否还在查找设备
     * @return
     */
    public Boolean isDiscoveringS(){
        boolean discovering = mAdapter.isDiscovering();
        return discovering;
    }

    /**
     * 得到配对的设备列表，清除已配对的设备，即全部解绑
     */
    public void removePairDevice(){
        if(mAdapter!=null){
            Set<BluetoothDevice> bondedDevices = mAdapter.getBondedDevices();
            for(BluetoothDevice device : bondedDevices ){
                unpairDevice(device);
            }
        }

    }

    /**
     * 反射来调用BluetoothDevice.removeBond取消设备的配对
     * @param device
     */
    public void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e("Test", e.getMessage());
        }
    }

    /**
     * 设置关闭永久可见
     */
    public void closeDiscoverableTimeout() {
        BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(adapter, 1);
            setScanMode.invoke(adapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE,1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过可见设置永久可见
     * 用于手机，蓝牙贴不可见
     * @param timeout
     */
    public void setDiscoverableTimeout(int timeout) {
        try {
            Method setDiscoverableTimeout = BluetoothAdapter.class.getMethod("setDiscoverableTimeout", int.class);
            setDiscoverableTimeout.setAccessible(true);
            Method setScanMode =BluetoothAdapter.class.getMethod("setScanMode", int.class,int.class);
            setScanMode.setAccessible(true);

            setDiscoverableTimeout.invoke(mAdapter, timeout);
            setScanMode.invoke(mAdapter, BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE,timeout);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
