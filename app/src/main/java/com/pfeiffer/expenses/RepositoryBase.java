package com.pfeiffer.expenses;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

class RepositoryBase {

    final ExpensesSQLiteHelper dbHelper_;
    SQLiteDatabase database_;

    RepositoryBase(ExpensesSQLiteHelper dbHelper) {
        dbHelper_ = dbHelper;
    }

    public void open() throws SQLException {
        database_ = dbHelper_.getWritableDatabase();
    }

    public void close() {
        dbHelper_.close();
    }

}
