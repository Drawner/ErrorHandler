package com.andrioussolutions.errorhandler.frmwrk.db;


import android.content.ContentValues;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;


/**
 * Copyright (C) 2016  Andrious Solutions Ltd.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or any later version.
 *
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
abstract public class dbDataRecords implements dbDataRecordsInt, DatabaseErrorHandler{


    public static ContentValues VALUES = new ContentValues();

    private long mId = -1; // unique ID for the alarm, only one alarm can be set for each item.

    private boolean mNewItem = false;

    private boolean mDeleted = false;

    private boolean mUseScript = false;



    abstract public String dbName();


    // Should be greater than 0
    abstract public int dbVersion();


    abstract public String DBKEY_FIELD();


    // Ported to the file, create.sql
    abstract public void onCreate(SQLiteDatabase db);


    // SQL statement used to upgrade the database.
    abstract public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);


    abstract public boolean ifNewRec();



    // Called in getDatabaseLocked(boolean writable) when a database is being opened.
    public void onConfigure(dbHelper helper){

    }



    // Called in getWritableDatabase() when a database has just been opened..
    public void onOpen(dbHelper helper){

    }



    // the method to be invoked when database corruption is detected.
    public void onCorruption(SQLiteDatabase db){

    }



    public long getId(){

        Object value = VALUES.get(DBKEY_FIELD());

        long id;

        if (value == null){

            id = -1;
        }else{

            id = ((Number) value).longValue();
        }

        return id;
    }



    public void setId(long id){

        VALUES.put(DBKEY_FIELD(), id);
    }



    public void setId(String id){

        mId = Integer.parseInt(id);
    }



    public boolean newItem(boolean newItem){

        mNewItem = newItem;

        return mNewItem;
    }



    public boolean newItem(){

        return mNewItem;
    }



    public boolean isDeleted(){

        return mDeleted;
    }



    public boolean isDeleted(boolean deleted){

        mDeleted = deleted;

        return mDeleted;
    }



    public String DATABASE_NAME(){

        return dbName();
    }



    public String DATABASE_FILE(){

        return dbFile();
    }



    public String dbFile(){

        String file = dbName();

        return file.isEmpty() ? "" : file + ".db";
    }



    public String SELECT_ALL(){

        return "SELECT " + DBKEY_FIELD() + " AS _id, * FROM " + DATABASE_NAME();
    }



    public String DROP_TABLE(){

        return "DROP TABLE IF EXISTS " + DATABASE_NAME();
    }



    public String CREATE_NOT_DELETED(){

        return "CREATE TEMP VIEW IF NOT EXISTS temp.notdeleted AS SELECT " + DBKEY_FIELD()
                + " AS _id, * FROM " + DATABASE_NAME() + " WHERE deleted = 0";
    }



    public String CREATE_DELETED(){

        return "CREATE TEMP VIEW IF NOT EXISTS temp.deleted AS SELECT " + DBKEY_FIELD()
                + " AS _id, * FROM " + DATABASE_NAME() + " WHERE deleted = 1";
    }



    public String DROP_DELETED(){

        return "DROP VIEW IF EXISTS temp.deleted";
    }



    public String SELECT_NOT_DELETED(){

        return "SELECT " + DBKEY_FIELD() + " AS _id, * FROM temp.notdeleted";
    }



    public String SELECT_DELETED(){

        return "SELECT " + DBKEY_FIELD() + " AS _id, * FROM temp.deleted";
    }



    public boolean useScript(){

        return mUseScript;
    }



    public boolean useScript(boolean use){

        mUseScript = use;

        return use;
    }



    public void onDestroy(){

    }
}
