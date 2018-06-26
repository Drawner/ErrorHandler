package com.andrioussolutions.errorhandler.db;

import com.andrioussolutions.errorhandler.frmwrk.db.dbDataRecords;
import com.andrioussolutions.errorhandler.frmwrk.db.dbHelper;

import android.content.Context;
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
public class dbRecError extends dbDataRecords{

    int mDBVersion = 4;

    private static dbHelper mDbHelper;


    public static dbHelper getDbHelper(Context context){

        if (mDbHelper == null){

            dbRecError db = new dbRecError();

            mDbHelper = dbHelper.GETINSTANCE(context, db, db.dbFile(), null, db.dbVersion(), db);
        }
        return mDbHelper;
    }


    @Override
    public String dbName(){

        return "errorLog";
    }

    @Override
    public int dbVersion(){

        return mDBVersion;
    }

    @Override
    public boolean ifNewRec(){

        return false;
    }


    @Override
    public String DBKEY_FIELD(){

        return "rowid";
    }


    @Override
    public void onCreate(SQLiteDatabase db){

        db.execSQL("CREATE TABLE IF NOT EXISTS " + DATABASE_NAME()
                + " (errMsg VARCHAR(100), errInfo TEXT (500), deleted integer default 0)");
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        db.execSQL("DROP TABLE IF EXISTS  " + DATABASE_NAME());

        onCreate(db);
    }


    public void onDestroy(){

        mDbHelper = null;
    }
}
