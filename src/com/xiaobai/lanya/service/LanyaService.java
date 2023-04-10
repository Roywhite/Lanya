package com.xiaobai.lanya.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.annotation.Nullable;

import com.xiaobai.lanya.util.BlueToothControllerUtil;

public class LanyaService extends IntentService {

    private BlueToothControllerUtil mController = new BlueToothControllerUtil();
    private SharedPreferences sp;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * name Used to name the worker thread, important only for debugging.
     */
    public LanyaService(){
        super("LanyaService");
    }
    public LanyaService(Context context) {
        super("LanyaService");
        sp = context.getSharedPreferences("lanya_device", MODE_MULTI_PROCESS);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        boolean supportBlueTooth = mController.isSupportBlueTooth();
        if(supportBlueTooth==true) {
            if (mController.getBlueToothStatus() == true) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        mController.findDevice();
                    }
                });
            }
        }
    }


}
