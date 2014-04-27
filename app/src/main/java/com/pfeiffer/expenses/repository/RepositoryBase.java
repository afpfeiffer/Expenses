package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;

class RepositoryBase {

    final ExpensesSQLiteHelper dbHelper_;
    SQLiteDatabase database_;
    final String deviceOwner_;

    RepositoryBase(Context context, ExpensesSQLiteHelper dbHelper) {
        deviceOwner_= Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        dbHelper_ = dbHelper;
    }

    public void open() throws SQLException {
        database_ = dbHelper_.getWritableDatabase();
    }

    public void close() {
        dbHelper_.close();
    }

}
