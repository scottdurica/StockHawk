package com.sam_chordas.android.stockhawk.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sam_chordas.android.stockhawk.rest.NetworkChangeEvent;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.greenrobot.eventbus.EventBus;

public class NetWorkStatusListener extends BroadcastReceiver {



    public NetWorkStatusListener() {
    }
    public static final String LOG_TAG = "NetworkStatusListener";

    @Override
    public void onReceive(Context context, Intent intent) {

        if (Utils.connectedToNetwork(context)){
            EventBus.getDefault().post(new NetworkChangeEvent(true));
        }else{
            EventBus.getDefault().post(new NetworkChangeEvent(false));
        }

    }


}
