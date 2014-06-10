package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ExpensesSQLiteHelper extends SQLiteOpenHelper {

    static ExpensesSQLiteHelper helperInstance_ = null;

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
    public static final String PURCHASE_ID_OWNER = "idOwner";

    private static final String CREATE_PURCHASE_TABLE = "create table " + TABLE_PURCHASE + "(" +
            PURCHASE_ID + " integer primary key autoincrement," + PURCHASE_BARCODE + " text," +
            PURCHASE_AMOUNT + " integer not null," + PURCHASE_DATE + " int," + PURCHASE_LOCATION +
            " text not null," + PURCHASE_PRICE + " text not null," + PURCHASE_CASH +
            " integer unsigned not null," + PURCHASE_PRODUCT_NAME + " text not null," +
            PURCHASE_CATEGORY + " text not null," + "" + PURCHASE_OWNER + " text not null," +
            PURCHASE_ID_OWNER + " integer);";

    public static final String TABLE_PURCHASE_TEMPLATE = "purchaseTemplates";
    public static final String PURCHASE_TEMPLATE_ID = "id";
    public static final String PURCHASE_TEMPLATE_AMOUNT = "amount";
    public static final String PURCHASE_TEMPLATE_LOCATION = "location";
    public static final String PURCHASE_TEMPLATE_PRICE = "price";
    public static final String PURCHASE_TEMPLATE_PRODUCT_NAME = "productName";
    public static final String PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES = "numberOfPurchases";
    public static final String PURCHASE_TEMPLATE_LAST_PURCHASE_DATE = "lastPurchase";
    public static final String PURCHASE_TEMPLATE_CATEGORY = "category";
    public static final String PURCHASE_TEMPLATE_CASH = "cash";


    private static final String CREATE_PURCHASE_TEMPLATE_TABLE = "create table " +
            TABLE_PURCHASE_TEMPLATE + "(" + PURCHASE_TEMPLATE_ID +
            " integer primary key autoincrement," + PURCHASE_TEMPLATE_AMOUNT + " integer not null,"
            + PURCHASE_TEMPLATE_LOCATION + " text not null," + PURCHASE_TEMPLATE_PRICE +
            " text not null," + PURCHASE_TEMPLATE_PRODUCT_NAME + " text not null," +
            PURCHASE_TEMPLATE_NUMBER_OF_PURCHASES + " integer not null," +
            PURCHASE_TEMPLATE_LAST_PURCHASE_DATE + " integer, " + PURCHASE_TEMPLATE_CATEGORY +
            " text not null," + PURCHASE_TEMPLATE_CASH +
            " integer unsigned not null);";


    public static final String TABLE_PARTNER_DEVICE = "partnerDevice";
    public static final String PARTNER_DEVICE_ID = "id";
    public static final String PARTNER_DEVICE_ANDROID_ID = "androidId";
    public static final String PARTNER_DEVICE_LAST_SYNCHRONIZATION = "lastSynchronization";

    public static final String CREATE_PARTNER_DEVICE_TABLE = "create table " + TABLE_PARTNER_DEVICE
            + "(" + PARTNER_DEVICE_ID + " integer primary key autoincrement," +
            PARTNER_DEVICE_ANDROID_ID + " text not null," + PARTNER_DEVICE_LAST_SYNCHRONIZATION +
            " integer);";

    public static final String TABLE_PURCHASE_HISTORY = "purchaseHistory";
    public static final String PURCHASE_HISTORY_ID = "id";
    public static final String PURCHASE_HISTORY_TIMESTAMP = "timestamp";
    public static final String PURCHASE_HISTORY_PURCHASE_ID = "purchaseId";
    public static final String PURCHASE_HISTORY_OPERATION = "operation";

    public static final String CREATE_PURCHASE_HISTORY_TABLE = "create table " +
            TABLE_PURCHASE_HISTORY + "(" + PURCHASE_HISTORY_ID +
            " integer primary key autoincrement," + PURCHASE_HISTORY_TIMESTAMP + " integer," +
            PURCHASE_HISTORY_PURCHASE_ID + " integer," + PURCHASE_HISTORY_OPERATION + " integer);";


    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 25;

    public ExpensesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static ExpensesSQLiteHelper getInstance(Context context) {
        if (helperInstance_ == null) {
            helperInstance_ = new ExpensesSQLiteHelper(context);
        }
        return helperInstance_;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(ExpensesSQLiteHelper.class.getName(), "Creating Database with SQL statement: " + CREATE_PURCHASE_TABLE +
                ", " + CREATE_PURCHASE_TEMPLATE_TABLE + ", " + CREATE_PARTNER_DEVICE_TABLE + ", " + CREATE_PURCHASE_HISTORY_TABLE);
        database.execSQL(CREATE_PURCHASE_TABLE);
        database.execSQL(CREATE_PURCHASE_TEMPLATE_TABLE);
        database.execSQL(CREATE_PARTNER_DEVICE_TABLE);
        database.execSQL(CREATE_PURCHASE_HISTORY_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        Log.w(ExpensesSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE);
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_TEMPLATE);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PARTNER_DEVICE);
//        database.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_HISTORY);


//        database.execSQL(CREATE_PURCHASE_TABLE);
        database.execSQL(CREATE_PURCHASE_TEMPLATE_TABLE);
//        database.execSQL(CREATE_PARTNER_DEVICE_TABLE);
//        database.execSQL(CREATE_PURCHASE_HISTORY_TABLE);


//        Old Changes:

//        from getAllPurchases()
//                for(Purchase purchase : purchases){
//                if(purchase.getCategory().equals(EnumCategory.OBST)){
//                Log.d("OBST+++", purchase.toString());
//                purchase.setCategory(EnumCategory.LEBENSMITTEL);
//                updatePurchase(purchase);
//            }
//        }
    }

}
