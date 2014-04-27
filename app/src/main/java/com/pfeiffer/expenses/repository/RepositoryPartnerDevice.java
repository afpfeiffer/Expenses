package com.pfeiffer.expenses.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.pfeiffer.expenses.model.PartnerDevice;

import java.util.Date;

/**
 * Created by axelpfeiffer on 26.04.14.
 */
public class RepositoryPartnerDevice extends RepositoryBase {
    private final String logTag_ = this.getClass().getName();

    private final static String[] allPartnerDeviceColumns_ = {ExpensesSQLiteHelper.PARTNER_DEVICE_ID,
            ExpensesSQLiteHelper.PARTNER_DEVICE_ANDROID_ID,
            ExpensesSQLiteHelper.PARTNER_DEVICE_LAST_SYNCHRONIZATION};

    public RepositoryPartnerDevice(ExpensesSQLiteHelper dbHelper) {
        super(dbHelper);
    }

    private PartnerDevice cursorToPartnerDevice(Cursor cursor) {
        if (cursor.isAfterLast())
            throw new IllegalStateException("Database cursor out of bounds.");
        return new PartnerDevice(cursor.getInt(0), cursor.getString(1), new Date(Long.parseLong(cursor.getString(2))));
    }

    long savePartnerDevice(PartnerDevice partnerDevice) {
        Log.d(logTag_, "savePartnerDevice(" + partnerDevice + ")");

        if (partnerDevice == null) {
            throw new IllegalArgumentException();
        }

        String androidId = partnerDevice.getAndroidId();
        Date lastSynchronization = partnerDevice.getLastSynchronization();

        if (androidId == null || lastSynchronization == null) {
            throw new IllegalArgumentException();
        }

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PARTNER_DEVICE_ANDROID_ID, androidId);
        values.put(ExpensesSQLiteHelper.PARTNER_DEVICE_LAST_SYNCHRONIZATION, lastSynchronization.getTime());

        return database_.insert(ExpensesSQLiteHelper.TABLE_PARTNER_DEVICE, null, values);
    }

    boolean updatePartnerDevice(PartnerDevice partnerDevice) {
        Log.d(logTag_, "updatePartnerDevice(" + partnerDevice + ")");

        if (partnerDevice == null) {
            throw new IllegalArgumentException();
        }

        int id = partnerDevice.getId();
        Date lastSynchronization = partnerDevice.getLastSynchronization();

        if (lastSynchronization == null) {
            throw new IllegalArgumentException();
        }

        ContentValues values = new ContentValues();
        values.put(ExpensesSQLiteHelper.PARTNER_DEVICE_LAST_SYNCHRONIZATION, lastSynchronization.getTime());

        int result = database_.update(ExpensesSQLiteHelper.TABLE_PARTNER_DEVICE, values,
                ExpensesSQLiteHelper.PARTNER_DEVICE_ID + "=" + id, null);
        boolean oneRowUpdated = (result == 1);
        Log.d(logTag_, "updatePartnerDevice returns" + oneRowUpdated);
        return oneRowUpdated; // true if one row was updated
    }


    public PartnerDevice findPartnerDeviceByAndroidId(String androidId) {
        Log.d(logTag_, "findPartnerDeviceByAndroidId(" + androidId + ")");

        Cursor cursor = database_.query(ExpensesSQLiteHelper.TABLE_PARTNER_DEVICE,
                allPartnerDeviceColumns_, " " + ExpensesSQLiteHelper.PARTNER_DEVICE_ANDROID_ID
                        + " = ?", new String[]{androidId}, null, null, null, null
        );

        cursor.moveToFirst();

        PartnerDevice partnerDevice = null;
        if (!cursor.isAfterLast()) {
            partnerDevice = cursorToPartnerDevice(cursor);

        }
        cursor.close();

        Log.d(logTag_, "findPartnerDeviceByAndroidId returns " + partnerDevice);
        return partnerDevice;
    }


}
