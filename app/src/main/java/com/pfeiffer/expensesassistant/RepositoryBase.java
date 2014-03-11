package com.pfeiffer.expensesassistant;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class RepositoryBase {

	protected ExpensesSQLiteHelper dbHelper_;
	protected SQLiteDatabase database_;

	public RepositoryBase( ExpensesSQLiteHelper dbHelper ) {
		dbHelper_ = dbHelper;
	}

	public void open() throws SQLException {
		database_ = dbHelper_.getWritableDatabase();
	}

	public void close() {
		dbHelper_.close();
	}

}
