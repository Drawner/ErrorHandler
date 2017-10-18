package com.gtfp.errorhandler;

//import com.gtfp.errorhandler.db.dbRecError;
//import com.gtfp.errorhandler.frmwrk.db.dbHelper;
//import com.gtfp.errorhandler.http.http;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

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


    private ErrorHandler(Activity activity){

//        mDBErrDatabase = ErrorDatabase(activity);

        mPackageName = getPackageName(activity);

//        mErrorCount = countErrors();
    }



    public static ErrorHandler getINSTANCE(Activity activity){

        if (mErrorHandler == null){

            mErrorHandler = new ErrorHandler(activity);
        }

        return mErrorHandler;
    }


    private static String getPackageName(Context pContext){

        String packageName = "";

        try{

            ActivityManager activityManager = (ActivityManager) pContext
                    .getSystemService(Context.ACTIVITY_SERVICE);

            if (Build.VERSION.SDK_INT > 20){

                packageName = activityManager.getRunningAppProcesses().get(0).processName;
            }else{

                // <uses-permission android:name="android.permission.GET_TASKS" />
                packageName = activityManager.getRunningTasks(1).get(0).topActivity
                        .getPackageName();
            }

            // There is a limit to the tag length of 23.
            packageName = packageName
                    .substring(0, packageName.length() > 22 ? 22 : packageName.length());

        }catch (Exception ex){
        }

        if (packageName.isEmpty()){
            packageName = pContext.getPackageName();
        }

        return packageName;
    }




    public static void toCatch(Activity activity){

        Thread.setDefaultUncaughtExceptionHandler(getINSTANCE(activity));
    }




    public static void logError(String message){

        if (message.isEmpty()){

            return;
        }

        logError(new Throwable(message.trim()));
    }




    public static void logError(Throwable exception){

        try{

            logCrash(exception);

        }catch (Exception e){

            Log.e(mPackageName, e.getMessage());
        }
    }

//    public static int countErrors(){
//
//        if (mDBErrDatabase.open().isOpen()){
//
//            Cursor recs = mDBErrDatabase.getRecs();
//
//            mErrorCount = recs.getCount();
//
//            recs.close();
//
//            mDBErrDatabase.close();
//        }
//
//        return mErrorCount;
//    }

//    private static dbHelper ErrorDatabase(Activity activity){
//
//        dbRecError dbRecords = new dbRecError();
//
//        return dbHelper.GETINSTANCE(activity, dbRecords, dbRecords.dbFile(), null,
//                    dbRecords.dbVersion(), dbRecords);
//    }




    // Return the last error message
    public static String getErrorMessage(){

        return mErrorMessage;
    }




    private static void setErrorMessage(String errMsg){

        mErrorMessage = errMsg;
    }

//    private static boolean recError(String errInfo){
//
//        boolean recErr = mDBErrDatabase.open().isOpen();
//
//        if (recErr){
//
//            dbRecError dbRecords = new dbRecError();
//
//            dbRecords.VALUES.put("errMsg", getErrorMessage());
//
//            dbRecords.VALUES.put("errInfo", errInfo);
//
//            recErr = mDBErrDatabase.save(dbRecords);
//
//            // Update the error count
//            mErrorCount = mDBErrDatabase.getRecs().getCount();
//        }
//
//        mDBErrDatabase.close();
//
//        return recErr;
//    }




    // Return the last crash information
    public static ApplicationErrorReport.CrashInfo crashInfo(){

        return mCrashInfo;
    }




    private static String getAppLabel(Context pContext){

        PackageManager lPackageManager = pContext.getPackageManager();

        ApplicationInfo lApplicationInfo = null;

        try{

            lApplicationInfo = lPackageManager
                    .getApplicationInfo(pContext.getApplicationInfo().packageName, 0);

        }catch (final PackageManager.NameNotFoundException e){
        }

        return (String) (lApplicationInfo != null ? lPackageManager
                .getApplicationLabel(lApplicationInfo) : "Unknown");
    }

//    public static int getErrorCount(){
//
//        return mErrorCount;
//    }




    public static boolean inDebugger(){

        //  If in Debugger Environment
        boolean debugging = Debug.isDebuggerConnected();

        return debugging;
    }

//    public static void sendErrorHttp(ArrayList<HashMap<String, String>> errorList){
//
//        String log = "";
//
//        for (int cnt = 0; cnt < errorList.size(); cnt++){
//
//            log = log + "\n" + errorList.get(cnt).get("errInfo");
//        }
//
//        if (!log.isEmpty())
//
//        {
//            http.post(log, "<YOUR WEBSITE>/errors.txt");
//        }
//    }




    @NonNull
    private static String errorMsg(Throwable exception, String exceptError){

        if (!exceptError.contains("error")){

            setErrorMessage(reportError(exception));

            mReportBuilder.append(getErrorMessage());
        }

        if (!exceptError.contains("callstack")){

            mReportBuilder.append(reportCallStack(exception));
        }

        if (!exceptError.contains("deviceinfo")){

            mReportBuilder.append(reportDeviceInfo());
        }

        if (!exceptError.contains("firmware")){

            mReportBuilder.append(reportFirmware());
        }

        return mReportBuilder.toString();
    }




    private static String reportError(Throwable exception){

        mCrashInfo = new ApplicationErrorReport.CrashInfo(exception);

        if (mCrashInfo.exceptionMessage == null){

            mErrorMessage = "<unknown error>";
        }else{

            mErrorMessage = mCrashInfo.exceptionMessage
                    .replace(": " + mCrashInfo.exceptionClassName, "");
        }

        String throwFile = mCrashInfo.throwFileName == null ? "<unknown file>"
                                                            : mCrashInfo.throwFileName;

        return "\n************ " + mCrashInfo.exceptionClassName + " ************\n"
                + mErrorMessage + LINE_SEPARATOR
                + "\n File: " + throwFile
                + "\n Method: " + mCrashInfo.throwMethodName + "()"
                + "\n Line No.: " + Integer.toString(mCrashInfo.throwLineNumber)
                + LINE_SEPARATOR;
        //			+ "Class: " + crashInfo.throwClassName + LINE_SEPARATOR
    }




    private static String reportCallStack(Throwable exception){

        StringWriter stackTrace = new StringWriter();

        exception.printStackTrace(new PrintWriter(stackTrace));

        String callStack = stackTrace.toString();

        String errMsg = exception.toString();

        return "\n************ CALLSTACK ************\n"
                + callStack.replace(errMsg, "")
                + LINE_SEPARATOR;
    }




    private static String reportDeviceInfo(){

        return "\n************ DEVICE INFORMATION ***********\n"
                + "Brand: "
                + Build.BRAND
                + LINE_SEPARATOR
                + "Device: "
                + Build.DEVICE
                + LINE_SEPARATOR
                + "Model: "
                + Build.MODEL
                + LINE_SEPARATOR
                + "Id: "
                + Build.ID
                + LINE_SEPARATOR
                + "Product: "
                + Build.PRODUCT
                + LINE_SEPARATOR;
    }




    private static String reportFirmware(){

        return "\n************ FIRMWARE ************\n"
                + "SDK: "
                + Build.VERSION.SDK_INT
                + LINE_SEPARATOR
                + "Release: "
                + Build.VERSION.RELEASE
                + LINE_SEPARATOR
                + "Incremental: "
                + Build.VERSION.INCREMENTAL
                + LINE_SEPARATOR;
    }




    // Empty the report as it is begin re-populated.
    private static void reportEmptied(){

        // No need to empty
        if (mReportBuilder.length() == 0){

            return;
        }

        mReportBuilder.setLength(0);

        mReportBuilder.trimToSize();
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        // Don't re-enter -- avoid infinite loops if crash-reporting crashes.
        if (mCrashing) return;

        mCrashing = true;

        catchException(thread, exception);

        defaultExceptionHandler(thread, exception);
    }




    public String catchException(Thread thread, Throwable exception){

        String errorMsg = "";

        try{

            errorMsg = logCrash(exception);

        }catch (Exception ex){

            Log.e(mPackageName, ex.getMessage());
        }

        return errorMsg;
    }




    public static void defaultExceptionHandler(Thread thread, Throwable exception){

        try{

            // Execute the old handler.
            if (mOldHandler != null){

                mOldHandler.uncaughtException(thread, exception);
            }

        }catch (Exception ex){

            Log.e(mPackageName, ex.getMessage());
        }
    }




    public static String logCrash(Throwable exception){

//        if (!Log.isLoggable(mPackageName, Log.ERROR)){
//
//            return;
//        }
//
        return errorMsg(exception, "deviceinfo firmware");
    }




    public void onDestroy(){

        mErrorHandler = null;

//        mDBErrDatabase.close();
//
//        mDBErrDatabase = null;
    }

    // Prevents infinite loops.
    private static volatile boolean mCrashing = false;

    private static final StringBuilder mReportBuilder = new StringBuilder();

    private static final String LINE_SEPARATOR = "\n";

    private static final UncaughtExceptionHandler mOldHandler = Thread
            .getDefaultUncaughtExceptionHandler();

    private static ErrorHandler mErrorHandler;

    private static String mPackageName;

    private static ApplicationErrorReport.CrashInfo mCrashInfo;

//    private static dbHelper mDBErrDatabase;

    private static String mErrorMessage = "";

//    private static int mErrorCount = 0;
}
