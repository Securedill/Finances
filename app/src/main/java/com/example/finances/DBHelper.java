package com.example.finances;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;


// макет базы данных
public class DBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Data";
    public static final String TABLE_CONTACTS = "sum";

    public static final String KEY_ID = "_id";
    public static final String KEY_DATE = "name";
    public static final String KEY_INCOME = "income";


    public DBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // создание БД со строками даты типа String и дохода типа integer
        db.execSQL("create table " + TABLE_CONTACTS + "(" + KEY_ID + " integer primary key," +
                KEY_DATE + " text, " + KEY_INCOME + " integer" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        // перезагрузка БД при изменении
        db.execSQL("drop table if exists " + TABLE_CONTACTS);
        onCreate(db);
    }
}
