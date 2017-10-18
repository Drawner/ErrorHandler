package com.andrious.errorhandler.database;

import com.gtfp.errorhandler.db.dbRecError;
import com.gtfp.errorhandler.frmwrk.db.dbHelper;

import android.app.Activity;
import android.database.Cursor;
/**
 * Copyright  2017  Andrious Solutions Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created 7/6/2017
 */
public class ErrorHandler implements
        java.lang.Thread.UncaughtExceptionHandler{

    private static com.andrious.errorhandler.database.ErrorHandler mErrorDatabase;

    private com.gtfp.errorhandler.ErrorHandler mErrorHandler;

    private static Thread mThread;

    private static Throwable mException;

    private static dbHelper mDBErrDatabase;

    private static int mErrorCount = 0;



    private ErrorHandler(Activity activity){

        mDBErrDatabase = ErrorDatabase(activity);

        mErrorHandler = com.gtfp.errorhandler.ErrorHandler.getINSTANCE(activity);

        mErrorCount = countErrors();
    }




    public static com.andrious.errorhandler.database.ErrorHandler getINSTANCE(Activity activity){

        if (mErrorDatabase == null){

            mErrorDatabase = new ErrorHandler(activity);
        }
        return mErrorDatabase;
    }




    public static void toCatch(Activity activity){

        if(mErrorDatabase == null){

            Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(activity));
        }
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        mThread = thread;

        mException = exception;

        String errorMsg = mErrorHandler.catchException(thread, exception);
    }




    public void catchException(Thread thread, Throwable exception){

        uncaughtException(thread, exception);
    }




    public static void defaultExceptionHandler(){

        if(mThread != null){

            com.gtfp.errorhandler.ErrorHandler.defaultExceptionHandler(mThread, mException);
        }
    }




    public static void defaultExceptionHandler(Thread thread, Throwable exception){

        com.gtfp.errorhandler.ErrorHandler.defaultExceptionHandler(thread, exception);
    }




    public static void logError(String message){

        com.gtfp.errorhandler.ErrorHandler.logError(message);
    }




    public static void logError(Throwable exception){

        com.gtfp.errorhandler.ErrorHandler.logError(exception);
    }




    public static boolean inDebugger(){

        return com.gtfp.errorhandler.ErrorHandler.inDebugger();
    }



    public static int countErrors(){

        if (mDBErrDatabase.open().isOpen()){

            Cursor recs = mDBErrDatabase.getRecs();

            mErrorCount = recs.getCount();

            recs.close();

            mDBErrDatabase.close();
        }

        return mErrorCount;
    }




    public static int getErrorCount(){

        return mErrorCount;
    }



    private static dbHelper ErrorDatabase(Activity activity){

        dbRecError dbRecords = new dbRecError();

        return dbHelper.GETINSTANCE(activity, dbRecords, dbRecords.dbFile(), null,
                    dbRecords.dbVersion(), dbRecords);
    }




    public void onDestroy(){

        mErrorHandler.onDestroy();

        mErrorHandler = null;

        mDBErrDatabase.close();

        mDBErrDatabase = null;
    }
}