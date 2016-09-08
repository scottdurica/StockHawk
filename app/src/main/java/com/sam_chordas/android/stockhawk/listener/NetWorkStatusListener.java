package com.sam_chordas.android.stockhawk.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sam_chordas.android.stockhawk.rest.NetworkChangeEvent;
import com.sam_chordas.android.stockhawk.rest.Utils;

import org.greenrobot.eventbus.EventBus;

public class NetWorkStatusListener extends BroadcastReceiver {



    public NetWorkStatusListener() {
    }
    public static final String LOG_TAG = "NetworkStatusListener";
    private boolean showWarning = false;

    @Override
    public void onReceive(Context context, Intent intent) {

//        if (Utils.connectedToNetwork(context)){
//
//            Intent serviceIntent = new Intent(context, StockIntentService.class);
//            serviceIntent.putExtra("tag","init");
//            context.startService(serviceIntent);
//            showWarning = false;
//
//        }else{
//            showWarning = true;
//        }
//
//        Log.e(LOG_TAG,"Calling bus...");
//        EventBus.getDefault().post(new NetworkChangeEvent(showWarning));
        if (Utils.connectedToNetwork(context)){
            EventBus.getDefault().post(new NetworkChangeEvent(true));


        }else{

            EventBus.getDefault().post(new NetworkChangeEvent(false));
        }

        Log.e(LOG_TAG,"Calling bus...");



    }


}
