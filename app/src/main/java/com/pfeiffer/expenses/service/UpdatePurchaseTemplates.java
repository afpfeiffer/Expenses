package com.pfeiffer.expenses.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by axelpfeiffer on 26.03.14.
 */
public class UpdatePurchaseTemplates extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //TODO do something useful
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
