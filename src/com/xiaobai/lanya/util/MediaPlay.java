package com.xiaobai.lanya.util;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;

import java.io.IOException;

public class MediaPlay {
    private MediaPlayer mPlay;// 播放音乐的类

    public  MediaPlay(Context context){
        mPlay = new MediaPlayer();
    }
    public void start(Context context) {
        // 获取assets目录
        AssetManager am = context.getAssets();
        try {
            // 得到文件
            AssetFileDescriptor afd = am.openFd("lanyashebeidiushi.mp3");
            // 装载资源
            //1..项目资源
            mPlay.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getDeclaredLength());
            // 正式加载资源
            mPlay.prepare();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // 开始播放
        mPlay.start();
    }
}
