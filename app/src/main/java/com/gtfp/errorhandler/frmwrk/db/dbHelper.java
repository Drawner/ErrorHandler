package com.gtfp.errorhandler.frmwrk.db;

import com.gtfp.errorhandler.ErrorHandler;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * Copyright (C) 2015  Greg T. F. Perry
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
public class dbHelper extends SQLiteOpenHelper{

    public static dbHelper mDBHelper;

    private dbDataRecords mdbRecords;

    private SQLiteDatabase mDB;

    private Context mContext;

    private Cursor mResultSet;

    private boolean mUseView;


    public static dbHelper GETINSTANCE(Context context, dbDataRecords rec, String dbFile,
            SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler){

        if (mDBHelper == null){

            mDBHelper = new dbHelper(context, rec, dbFile, factory, version, errorHandler);
        }
        return mDBHelper;
    }


    private dbHelper(Context context, dbDataRecords rec, String dbFile,
            SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler){
        super(context, dbFile, factory, version, errorHandler);

        mdbRecords = rec;

        mContext = context;
    }


    public boolean createCurrentRecs(){

        try{

            mDB.execSQL(mdbRecords.CREATE_NOT_DELETED());

            mUseView = true;

        }catch (SQLException ex){

            return false;
        }

        return true;
    }


    public dbHelper open(){

        try{

            if (!isOpen()){

                mDB = getWritableDatabase();
            }

        }catch (SQLException ex){

            if (mDB != null && mDB.isOpen()){

                mDB.close();
            }

            if (mDB != null){

                mDB = null;
            }
        }

        return this;
    }


    public boolean isOpen(){

        return mDB != null && mDB.isOpen();
    }


    public void close(){
        super.close();

        // It's good to lose the reference here with the connection closed.
        mDB = null;

        if (mResultSet != null){
            mResultSet.close();
        }

        // This resource too. It could be huge!
        mResultSet = null;
    }


    public SQLiteDatabase getDatabase(){

        return mDB;
    }

    public void clearResultSet(){

        mResultSet = null;
    }


    public Cursor getResultSet(){

        if (mResultSet == null){

            if (mUseView){

                mResultSet = getCurrentRecs();
            }else{

                mResultSet = getRecs();
            }
        }

        return mResultSet;
    }


    public boolean showDeleted(boolean showDeleted){

        // Show deleted records means NOT to use the view.
        mUseView = !showDeleted;

        return showDeleted;
    }


    public Cursor runQuery(String sqlStmt){

        Cursor records;

        try{

            records = mDB.rawQuery(sqlStmt, null);

        }catch (RuntimeException ex){

            // If something goes wrong, return an empty cursor.
            records = new MatrixCursor(new String[]{"empty"});
        }

        return records;
    }


    public Cursor getRecs(){

        return runQuery(mdbRecords.SELECT_ALL());
    }

    public Cursor getCurrentRecs(){

        return runQuery(mdbRecords.SELECT_NOT_DELETED());
    }

    public Cursor getDeletedRecs(){

        return runQuery(mdbRecords.SELECT_DELETED());
    }


    public long getLastRowID(){

        Cursor records;

        final String SELECT_LASTROWID = "SELECT " + mdbRecords.DBKEY_FIELD() + " AS _id, * FROM "
                + mdbRecords.DATABASE_NAME()
                + " ORDER BY " + mdbRecords.DBKEY_FIELD() + " DESC LIMIT 1";

        records = runQuery(SELECT_LASTROWID);

        long lastRowID = 0;

        // You've got to move the cursor's position pointer after the query.
        if (records.moveToFirst()){

            try{

                lastRowID = records.getLong(0);

            }catch (RuntimeException ex){

                lastRowID = 0;
            }
        }

        return lastRowID;
    }


    public ArrayList<HashMap<String, String>> recArrayList(Cursor recs){

        ArrayList<HashMap<String, String>> list = new ArrayList<>();

        String value;

        while (recs.moveToNext()){

            HashMap<String, String> row = new HashMap<>();

            //iterate over the columns
            for (int col = 0; col < recs.getColumnNames().length; col++){

                value = recs.getString(col);

                if (value == null){

                    // Type String
                    if (recs.getType(col) == 3){

                        value = "";
                    }else{

                        value = "-1";
                    }
                }

                row.put(recs.getColumnName(col), value);
            }

            list.add(row);
        }

        return list;
    }


    public int deleteRec(Long rowID){

        String id = Long.toString(rowID);

        String where = mdbRecords.DBKEY_FIELD() + "=?";

        try{

            return mDB.delete(mdbRecords.DATABASE_NAME(), where, new String[]{id});

        }catch (RuntimeException ex){

            ErrorHandler.logError(ex);

            return -1;
        }
    }


    public boolean markRec(Long rowID){

        String id = Long.toString(rowID);

        String sql = "UPDATE OR FAIL " + mdbRecords.DATABASE_NAME() + " SET deleted = 1 WHERE "
                + mdbRecords.DBKEY_FIELD() + "=" + id;

        try{

            mDB.execSQL(sql);

        }catch (SQLException ex){

            ErrorHandler.logError(ex);

            return false;
        }

        return true;
    }


    public boolean save(dbDataRecords rec){

        long rowId = rec.getId();

        if (rowId > 0){

            return updateRec(rec) > 0;
        }else{

            rowId = insertRec(rec);

            if (rowId < 0){
                // Note, there was an error with a negative number.
                // but will be set to zero in the variable.
                rowId = 0;
            }

            return rowId > 0;
        }
    }


    public boolean ifNewRec(){

        return mdbRecords.ifNewRec();
    }


    private int updateRec(dbDataRecords rec){

        int result;

        try{

            result = mDB.update(rec.DATABASE_NAME(), rec.VALUES, rec.DBKEY_FIELD() + " = ?",
                    new String[]{String.valueOf(rec.getId())});

        }catch (RuntimeException ex){

            ErrorHandler.logError(ex);

            result = 0;
        }

        return result;
    }


    private long insertRec(dbDataRecords rec){

        long result;

        try{

            result = mDB.insert(rec.DATABASE_NAME(), null, rec.VALUES);

        }catch (Exception ex){

            result = 0;
        }

        return result;
    }


    public int setBindRecInt(String value){

        if (value == null || value.isEmpty()){
            return 0;
        }

        try{

            return Integer.parseInt(value);

        }catch (NumberFormatException ex){

            return 0;
        }
    }


    public long setBindRecLong(String value){

        if (value == null || value.isEmpty()){
            return 0L;
        }

        try{

            return Long.parseLong(value, 10);

        }catch (NumberFormatException ex){

            return 0L;
        }
    }


    public boolean importRec(){

        return insertRec(mdbRecords) > 0;
    }


    public Cursor getRecs(String whereClause){

        String sqlStmt = "SELECT " + mdbRecords.DBKEY_FIELD() + " AS _id, * FROM " + mdbRecords
                .DATABASE_NAME();

        sqlStmt = sqlStmt + " WHERE " + whereClause;

        Cursor resultSet;

        try{

            resultSet = mDB.rawQuery(sqlStmt, null);

        }catch (SQLException ex){

            // If something goes wrong, return an empty cursor.
            resultSet = new MatrixCursor(new String[]{"empty"});
        }

        return resultSet;
    }


    public boolean dropTable(){

        boolean dropped;

        try{

            mDB.execSQL("DROP TABLE IF EXISTS " + mdbRecords.DATABASE_NAME());

            dropped = true;

        }catch (SQLException ex){

            dropped = false;
        }

        return dropped;
    }


    @Override
    // Called in getDatabaseLocked(boolean writable) when a database is being opened.
    public void onConfigure(SQLiteDatabase db){

        mdbRecords.onConfigure(this);
    }

    @Override
    // Called in getWritableDatabase() when a database has just been opened..
    public void onOpen(SQLiteDatabase db){

        mdbRecords.onOpen(this);
    }


    public AssetManager getAssetManager(){

        return mContext.getAssets();
    }


    protected void execSqlFile(String sqlFile, SQLiteDatabase db) throws SQLException, IOException{

        int line = 0;

        for (String sqlInstruction : dbSQLParser
                .parseSqlFile(SQL_DIR + File.separator + sqlFile, this)){

            line = line + 1;

            if (line == 1 && sqlInstruction.substring(0, 7).equals("ATTACH ")){

                db.endTransaction();
            }

            db.execSQL(sqlInstruction);
        }
    }

    private static final String SQL_DIR = "sql";

    private static final String CREATEFILE = "create.sql";

    private static final String UPGRADEFILE_PREFIX = "upgrade-";

    private static final String UPGRADEFILE_SUFFIX = ".sql";


    // Called when no database exists or if there is a new 'version' indicated.
    @Override
    public void onCreate(SQLiteDatabase db){

        try{

            if (!mdbRecords.useScript()){

                mdbRecords.onCreate(db);
            }else{

                execSqlFile(CREATEFILE, db);
            }

        }catch (Exception ex){

            ErrorHandler.logError(ex);
        }
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){

        setTheGrade(db, oldVersion, newVersion);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){

        setTheGrade(db, oldVersion, newVersion);
    }


    private void setTheGrade(SQLiteDatabase db, int oldVersion, int newVersion){

        try{

            if (!mdbRecords.useScript()){

                mdbRecords.onUpgrade(db, oldVersion, newVersion);
            }else{

                for (String sqlFile : dbSQLParser.list(SQL_DIR, this)){

                    if (sqlFile.startsWith(UPGRADEFILE_PREFIX)){

                        int fileVersion = Integer.parseInt(
                                sqlFile.substring(UPGRADEFILE_PREFIX.length(),
                                        sqlFile.length() - UPGRADEFILE_SUFFIX.length()));

                        if (fileVersion > oldVersion && fileVersion <= newVersion){

                            execSqlFile(sqlFile, db);
                        }
                    }
                }
            }
        }catch (IOException ex){

            ErrorHandler.logError(ex);

        }catch (RuntimeException ex){

            // TODO Should notify the application the startup failed.
            ErrorHandler.logError(ex);

            // Be sure to throw the exception back up to be handled there as well.
            throw ex;
        }
    }

//    // Called when there is a database version mismatch meaning that the version
//    // of the database needs to be upgraded to the current version.
//    // Upgrade the existing database to conform to the new version. Multiple
//    // previous versions can be handled by comparing _oldVersion and _newVersion.
//    // The simplest case is to drop the old table and create a new one.    @Override
//    public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
//
//        if (_oldVersion >= _newVersion) {
//
//            Log.w(mContext.getClass().getSimpleName(),
//                    "Cannot 'upgrade' from version " + _newVersion + " to " + _oldVersion
//                            + ". Upgrade attempt failed.");
//        }
//
//        try {
//
//            _db.execSQL(ALTER_TABLE);
//
//        } catch (RuntimeException ex) {
//            // TODO Modify ErrorHandler to log the error directly.
//            Log.e(mContext.getClass().getSimpleName(),
//                    "Database upgrade failed. Version " + _oldVersion + " to " + _newVersion);
//
//            // Be sure to throw the exception back up to be handled there as well.
//            throw ex;
//        }
//
//        // Log the version upgrade.
//        Log.i(mContext.getClass().getSimpleName(),
//                "Database upgrade. Version " + _oldVersion + " to " + _newVersion);
//    }


    public void onDestroy(){

        close();

        mDBHelper = null;

        // Just making sure.
        mDB = null;

        mContext = null;

        if (mResultSet != null){
            mResultSet.close();
        }

        // This could be huge!
        mResultSet = null;
    }

    @SuppressWarnings("unchecked")
    public ArrayList<HashMap<String, String>> listRecs(){

        ArrayList<HashMap<String, String>> listOfErrors;

        if (dbHelper.this.open().isOpen()){

            listOfErrors = this.recArrayList(this.getRecs());

            dbHelper.this.close();
        }else{

            listOfErrors = new ArrayList();
            listOfErrors.add(new HashMap<String,String>());
        }

        return listOfErrors;
    }
}