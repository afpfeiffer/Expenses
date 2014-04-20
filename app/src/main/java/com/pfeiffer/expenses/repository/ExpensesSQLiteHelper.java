package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExpensesSQLiteHelper extends SQLiteOpenHelper {

    // product table definitions
    public static final String TABLE_PRODUCT = "product";
    public static final String PRODUCT_ID = "id";
    public static final String PRODUCT_NAME = "name";
    public static final String PRODUCT_BARCODE = "barcode";
    // Database creation sql statements
    private static final String CREATE_PRODUCT_TABLE = "create table " + TABLE_PRODUCT + "(" + PRODUCT_ID
            + " integer primary key autoincrement, " + PRODUCT_NAME + " text not null," + PRODUCT_BARCODE + " text" + " );";
    // purchase table definitions
    public static final String TABLE_PURCHASE = "purchase";
    public static final String PURCHASE_ID = "id";
    public static final String PURCHASE_PRODUCT_ID = "productId";
    public static final String PURCHASE_AMOUNT = "amount";
    public static final String PURCHASE_DATE = "date";
    public static final String PURCHASE_LOCATION = "location";
    public static final String PURCHASE_PRICE = "totalPrice";
    public static final String PURCHASE_CASH = "cash";
    public static final String PURCHASE_PRODUCT_NAME = "productName";
    public static final String PURCHASE_CATEGORY = "category";

    private static final String CREATE_PURCHASE_TABLE = "create table " + TABLE_PURCHASE + "(" + PURCHASE_ID
            + " integer primary key autoincrement, " + PURCHASE_PRODUCT_ID + " integer not null," + PURCHASE_AMOUNT
            + " integer not null," + PURCHASE_DATE + " int," + PURCHASE_LOCATION + " text not null,"
            + PURCHASE_PRICE + " text not null," + PURCHASE_CASH + " int unsigned not null," +
            PURCHASE_PRODUCT_NAME+" text not null,"+PURCHASE_CATEGORY+" text not null );";

    public static final String TABLE_PURCHASE_TEMPLATE = "purchaseTemplates";
    public static final String PURCHASE_TEMPLATE_ID = "id";
    public static final String PURCHASE_TEMPLATE_PRODUCT_ID = "productId";
    public static final String PURCHASE_TEMPLATE_AMOUNT = "amount";
    public static final String PURCHASE_TEMPLATE_LOCATION = "location";
    public static final String PURCHASE_TEMPLATE_PRICE = "totalPrice";
    public static final String PURCHASE_TEMPLATE_PRODUCT_NAME = "productName";
    public static final String PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES = "numberOfPurchases";
    public static final String PURCHASE_TEMPLATE_LAST_PURCHASE_DATE = "lastPurchase";

    private static final String CREATE_PURCHASE_TEMPLATE_TABLE = "create table " + TABLE_PURCHASE_TEMPLATE + "(" + PURCHASE_TEMPLATE_ID
            + " integer primary key autoincrement, " + PURCHASE_TEMPLATE_PRODUCT_ID + " integer not null," + PURCHASE_TEMPLATE_AMOUNT
            + " integer not null," + PURCHASE_TEMPLATE_LOCATION + " text not null,"
            + PURCHASE_TEMPLATE_PRICE + " text not null,"+PURCHASE_TEMPLATE_PRODUCT_NAME + " text not null," +
            "" + PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES + " integer not null,"+ PURCHASE_TEMPLATE_LAST_PURCHASE_DATE +" " +
            "int);";




    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 13;

    public ExpensesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(ExpensesSQLiteHelper.class.getName(), "Creating Database with SQL statement: " + CREATE_PRODUCT_TABLE
                + ", " + CREATE_PURCHASE_TABLE+ ", "+ CREATE_PURCHASE_TEMPLATE_TABLE);
        database.execSQL(CREATE_PRODUCT_TABLE);
        database.execSQL(CREATE_PURCHASE_TABLE);
        database.execSQL(CREATE_PURCHASE_TEMPLATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExpensesSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_TEMPLATE);
        onCreate(db);
    }

}
