package com.pfeiffer.expenses.repository;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ExpensesSQLiteHelper extends SQLiteOpenHelper {

    // product table definitions
    public static final String TABLE_PRODUCT = "product";
    public static final String PRODUCT_ID = "id";
    public static final String PRODUCT_NAME = "name";
    public static final String PRODUCT_CATEGORY = "category";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_BARCODE = "barcode";
    // Database creation sql statements
    private static final String CREATE_PRODUCT_TABLE = "create table " + TABLE_PRODUCT + "(" + PRODUCT_ID
            + " integer primary key autoincrement, " + PRODUCT_NAME + " text not null," + PRODUCT_CATEGORY
            + " text not null," + PRODUCT_PRICE + " text not null," + PRODUCT_BARCODE + " text" + " );";
    // purchase table definitions
    public static final String TABLE_PURCHASE = "purchase";
    public static final String PURCHASE_ID = "id";
    public static final String PURCHASE_PRODUCT_ID = "productId";
    public static final String PURCHASE_AMOUNT = "amount";
    public static final String PURCHASE_DATE = "date";
    public static final String PURCHASE_LOCATION = "location";
    public static final String PURCHASE_PRICE = "totalPrice";
    public static final String PURCHASE_CASH = "cash";
    private static final String CREATE_PURCHASE_TABLE = "create table " + TABLE_PURCHASE + "(" + PURCHASE_ID
            + " integer primary key autoincrement, " + PURCHASE_PRODUCT_ID + " integer not null," + PURCHASE_AMOUNT
            + " integer not null," + PURCHASE_DATE + " text not null," + PURCHASE_LOCATION + " text not null,"
            + PURCHASE_PRICE + " text not null," + PURCHASE_CASH + " int unsigned not null);";

    public static final String TABLE_PURCHASE_TEMPLATES = "purchaseTemplates";
    public static final String PURCHASE_TEMPLATES_ID = "id";
    public static final String PURCHASE_TEMPLATES_PRODUCT_ID = "productId";
    public static final String PURCHASE_TEMPLATES_AMOUNT = "amount";
    public static final String PURCHASE_TEMPLATES_LOCATION = "location";
    public static final String PURCHASE_TEMPLATES_PRICE = "totalPrice";
    private static final String CREATE_PURCHASE_TEMPLATES_TABLE = "create table " + TABLE_PURCHASE_TEMPLATES  + "(" + PURCHASE_TEMPLATES_ID
            + " integer primary key autoincrement, " + PURCHASE_TEMPLATES_PRODUCT_ID + " integer not null," + PURCHASE_TEMPLATES_AMOUNT
            + " integer not null," + PURCHASE_TEMPLATES_LOCATION + " text not null,"
            + PURCHASE_TEMPLATES_PRICE + " text not null);";




    private static final String DATABASE_NAME = "expenses.db";
    private static final int DATABASE_VERSION = 8;

    public ExpensesSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        Log.i(ExpensesSQLiteHelper.class.getName(), "Creating Database with SQL statement: " + CREATE_PRODUCT_TABLE
                + ", " + CREATE_PURCHASE_TABLE+ ", "+CREATE_PURCHASE_TEMPLATES_TABLE);
//        database.execSQL(CREATE_PRODUCT_TABLE);
//        database.execSQL(CREATE_PURCHASE_TABLE);
        database.execSQL(CREATE_PURCHASE_TEMPLATES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(ExpensesSQLiteHelper.class.getName(), "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCT);
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PURCHASE_TEMPLATES);
        onCreate(db);
    }

    public class QueryBuilder {
        final List<String> args_;
        String whereClause_;

        public QueryBuilder() {
            whereClause_ = "";
            args_ = new ArrayList<String>();
        }

        public void addWhere(String field, String value) {
            if (!whereClause_.equals(""))
                whereClause_ += " AND ";
            whereClause_ += field + "=?";
            args_.add(value);
        }

        public String getWhere() {
            if (whereClause_.equals(""))
                throw new IllegalStateException("Returning an empty where clause is illegal.");
            return " " + whereClause_;
        }

        public String[] getWhereArgs() {
            String[] ret = new String[args_.size()];
            ret = args_.toArray(ret);
            return ret;
        }
    }

}
