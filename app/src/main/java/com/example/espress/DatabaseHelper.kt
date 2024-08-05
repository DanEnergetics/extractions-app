package com.example.espress

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context?) : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable = "CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " TEXT, " +
                COL_2 + " TEXT, " +
                COL_3 + " TEXT, " +
                COL_4 + " TEXT)"
        db.execSQL(createTable)
    }

    fun deleteData(value1: String, value2: String, value3: String, time: String): Boolean {
        val db = this.writableDatabase
        val selection =
            COL_1 + " = ? AND " + COL_2 + " = ? AND " + COL_3 + " = ? AND " + COL_4 + " = ?"
        val selectionArgs = arrayOf(value1, value2, value3, time)
        val deletedRows = db.delete(TABLE_NAME, selection, selectionArgs)
        return deletedRows > 0
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME)
        onCreate(db)
    }

    fun insertData(value1: String?, value2: String?, value3: String?, time: String?): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(COL_1, value1)
        contentValues.put(COL_2, value2)
        contentValues.put(COL_3, value3)
        contentValues.put(COL_4, time)
        val result = db.insert(TABLE_NAME, null, contentValues)
        return result != -1L
    }

    val data: List<Array<String>>
        get() {
//    public Cursor getData() {
            val dataList: MutableList<Array<String>> = ArrayList()
            val db = this.writableDatabase
            val cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null)

//        return cursor;
            if (cursor.moveToFirst()) {
                do {
                    @SuppressLint("Range") val value1 =
                        cursor.getString(cursor.getColumnIndex(COL_1))
                    @SuppressLint("Range") val value2 =
                        cursor.getString(cursor.getColumnIndex(COL_2))
                    @SuppressLint("Range") val value3 =
                        cursor.getString(cursor.getColumnIndex(COL_3))
                    @SuppressLint("Range") val time = cursor.getString(cursor.getColumnIndex(COL_4))
                    val data = arrayOf(value1, value2, value3, time)
                    dataList.add(data)
                } while (cursor.moveToNext())
            }
            cursor.close()
            return dataList
        }

    companion object {
        private const val DATABASE_NAME = "data.db"
        private const val TABLE_NAME = "data_table"
        private const val COL_1 = "VALUE1"
        private const val COL_2 = "VALUE2"
        private const val COL_3 = "VALUE3"
        private const val COL_4 = "TIME"
    }
}
