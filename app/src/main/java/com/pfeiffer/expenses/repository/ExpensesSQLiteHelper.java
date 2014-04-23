package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExpensesSQLiteHelper extends SQLiteOpenHelper {

    // purchase table definitions
    public static final String TABLE_PURCHASE = "purchase";
    public static final String PURCHASE_ID = "id";
    public static final String PURCHASE_BARCODE = "barcode";
    public static final String PURCHASE_AMOUNT = "amount";
    public static final String PURCHASE_DATE = "date";
    public static final String PURCHASE_LOCATION = "location";
    public static final String PURCHASE_PRICE = "price";
    public static final String PURCHASE_CASH = "cash";
    public static final String PURCHASE_PRODUCT_NAME = "productName";
    public static final String PURCHASE_CATEGORY = "category";
    public static final String PURCHASE_OWNER = "owner";

    private static final String CREATE_PURCHASE_TABLE = "create table " + TABLE_PURCHASE + "(" + PURCHASE_ID
            + " integer primary key autoincrement, " + PURCHASE_BARCODE + " text," + PURCHASE_AMOUNT
            + " integer not null," + PURCHASE_DATE + " int," + PURCHASE_LOCATION + " text not null,"
            + PURCHASE_PRICE + " text not null," + PURCHASE_CASH + " integer unsigned not null," +
            PURCHASE_PRODUCT_NAME+" text not null,"+PURCHASE_CATEGORY+" text not null," +
            ""+PURCHASE_OWNER+" text not null);";

    public static final String TABLE_PURCHASE_TEMPLATE = "purchaseTemplates";
    public static final String PURCHASE_TEMPLATE_ID = "id";
    public static final String PURCHASE_TEMPLATE_AMOUNT = "amount";
    public static final String PURCHASE_TEMPLATE_LOCATION = "location";
    public static final String PURCHASE_TEMPLATE_PRICE = "price";
    public static final String PURCHASE_TEMPLATE_PRODUCT_NAME = "productName";
    public static final String PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES = "numberOfPurchases";
    public static final String PURCHASE_TEMPLATE_LAST_PURCHASE_DATE = "lastPurchase";
    public static final String PURCHASE_TEMPLATE_CATEGORY = "category";


    private static final String CREATE_PURCHASE_TEMPLATE_TABLE = "create table " + TABLE_PURCHASE_TEMPLATE + "(" + PURCHASE_TEMPLATE_ID
            + " integer primary key autoincrement, " + PURCHASE_TEMPLATE_AMOUNT
            + " integer not null," + PURCHASE_TEMPLATE_LOCATION + " text not null,"
            + PURCHASE_TEMPLATE_PRICE + " text not null,"+PURCHASE_TEMPLATE_PRODUCT_NAME + " text not null," +
            "" + PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES + " integer not null,"+ PURCHASE_TEMPLATE_LAST_PURCHASE_DATE +" " +
            "integer, "+PURCHASE_TEMPLATE_CATEGORY+" text not null);";




    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 19;

    public ExpensesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(ExpensesSQLiteHelper.class.getName(), "Creating Database with SQL statement: " + CREATE_PURCHASE_TABLE+
                ", "+ CREATE_PURCHASE_TEMPLATE_TABLE);
        database.execSQL(CREATE_PURCHASE_TABLE);
        database.execSQL(CREATE_PURCHASE_TEMPLATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExpensesSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_TEMPLATE);
        onCreate(db);
    }

}
