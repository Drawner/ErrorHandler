package com.gtfp.errorhandler;

import com.google.firebase.crash.FirebaseCrash;

import com.gtfp.errorhandler.db.dbRecError;
import com.gtfp.errorhandler.frmwrk.db.dbHelper;
import com.gtfp.errorhandler.http.http;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ApplicationErrorReport;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.widget.Toast;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ErrorHandler implements
        java.lang.Thread.UncaughtExceptionHandler{

    private static final UncaughtExceptionHandler oldHandler = Thread
            .getDefaultUncaughtExceptionHandler();

    private static final String RECIPIENT = "gtfperry@gmail.com";

    private static Activity myActivity;

    private static Throwable mException;

    private static ApplicationErrorReport.CrashInfo mCrashInfo;

    private static dbHelper mDBErrDatabase;

    private static String mErrorMessage = "";

    private static int mErrorCount = 0;

    private static String mPrevExcept = "";

    private static Thread mThread;



    private ErrorHandler(Activity activity){

        myActivity = activity;

        mErrorCount = countErrors();
    }



    public static void toCatch(Activity activity){

        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(activity));
    }



    public static void displayCrash(Throwable exception){

//        // Empty the report builder in case it's not
//        reportErrorHelper.reportEmptied();
//
//        reportErrorHelper.reportException(exception);
//
//        reportErrorHelper.reportCallStack(exception);
//
//        // reportErrorHelper.reportDeviceInfo();
//
//        reportErrorHelper.reportFirmware();

//        Intent intent = new Intent(myActivity, CrashActivity.class);
//
//        intent.putExtra("error", errMsg);
//
//        myActivity.startActivity(intent);

        Fragment fragment = new CrashFragment();

        final Bundle args = new Bundle();

        args.putString("error", reportErrorHelper.errorMsg());

        fragment.setArguments(args);

        runFragment(fragment, android.R.id.content);
    }



    public static void logError(String message){

        if (message.isEmpty()){

            return;
        }

        logError(new Throwable(message.trim()));
    }



    public static void logError(Throwable exception){

        mException = exception;

        mPrevExcept = "";

//        reportErrorHelper.reportError(exception);
//
//        reportErrorHelper.reportCallStack(exception);
//
//        // reportErrorHelper.reportDeviceInfo();
//
//        reportErrorHelper.reportFirmware();

        reportErrorHelper.clearReports();

        try{

            logCrash();

        }catch (Exception e){

            Log.e(getPackageName(myActivity), e.getMessage());

        }finally{

            mException = null;
        }
    }



    public static void logCrash(){

//        String contextName = getPackageName(myActivity);
//
//        Boolean loggable = Log.isLoggable(contextName, Log.ERROR);
//
//        if (!loggable){
//
//            return;
//        }

//        // Empty the report builder in case it's not
//        reportErrorHelper.reportEmptied();
//
//        reportErrorHelper.reportError(exception);
//
//        reportErrorHelper.reportCallStack(exception);
//
//        // reportErrorHelper.reportDeviceInfo();
//
//        reportErrorHelper.reportFirmware();

        if (inDebugger()){

//            String errInfo = reportErrorHelper.errorMsg("deviceinfo");

//**  No need to record that in the logcat.
//        Log.e(getPackageName(myActivity), errInfo);

//            // Record to a database.
//            recError(errInfo);
        }else{

            FirebaseCrash.report(mException);
        }
    }



    private static boolean recError(String errInfo){

        dbHelper db = ErrorDatabase();

        boolean recErr = db.open().isOpen();

        if (recErr){

            dbRecError dbRecords = new dbRecError();

            dbRecords.VALUES.put("errMsg", getErrorMessage());

            dbRecords.VALUES.put("errInfo", errInfo);

            recErr = db.save(dbRecords);

            // Update the error count
            mErrorCount = db.getRecs().getCount();
        }

        db.close();

        return recErr;
    }



    public static dbHelper ErrorDatabase(){

        if (mDBErrDatabase == null){

            dbRecError dbRecords = new dbRecError();

            mDBErrDatabase = dbHelper.GETINSTANCE(myActivity, dbRecords, dbRecords.dbFile(), null,
                    dbRecords.dbVersion(), dbRecords);
        }
        return mDBErrDatabase;
    }



    public static int countErrors(){

        dbHelper db = ErrorDatabase();

        if (db.open().isOpen()){

            Cursor recs = db.getRecs();

            mErrorCount = recs.getCount();

            recs.close();

            db.close();
        }

        return mErrorCount;
    }



    // Return the last error message
    public static String getErrorMessage(){

        return mErrorMessage;
    }



    public static void setErrorMessage(String errMsg){

        mErrorMessage = errMsg;
    }



    public static int getErrorCount(){

        return mErrorCount;
    }



    private static boolean runFragment(Fragment newFragment, int layoutID){

        try{

            String fragName = newFragment.getClass().getSimpleName();

            FragmentTransaction ft = myActivity.getFragmentManager()
                    .beginTransaction()
                    .addToBackStack(fragName)
                    .replace(layoutID, newFragment, fragName);

            ft.commit();

            return true;

        }catch (RuntimeException ex){

            return false;
        }
    }



    // Return the last crash information
    public static ApplicationErrorReport.CrashInfo crashInfo(){

        return mCrashInfo;
    }



    public static void sendErrorMail(ArrayList<HashMap<String, String>> errorList){

        String subject = "Errors!";

        String body = "";

        for (int cnt = 0; cnt < errorList.size(); cnt++){

            body = body + "\n\n" + errorList.get(cnt).get("errInfo");
        }

        sendErrorMail(subject, body);
    }



    public static void sendErrorMail(String subject, String body){

        sendErrorMail(subject, body, null);
    }



    public static boolean sendErrorMail(String subject, String body, List<Intent> intents){

        boolean send;

        Intent sendIntent = intentErrorMail(subject, body);

        if (intents == null){

            send = true;

            //sendIntent = Intent.createChooser(sendIntent, "Error Report");

            sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            myActivity.startActivity(sendIntent);
        }else{

            send = intents.add(sendIntent);
        }

        return send;
    }



    private static Intent intentErrorMail(String subject, String body){

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);

//        sendIntent.setData(Uri.parse("mailto:"));
//
//        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{RECIPIENT});

        sendIntent.setData(Uri.fromParts("mailto", RECIPIENT, null));

        sendIntent.setType("message/rfc822");

        sendIntent.putExtra(Intent.EXTRA_TEXT, body); //reportErrorHelper.errorMsg("DeviceInfo"));

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject.trim()); //"app_name crashed! Fix it!");

        return sendIntent;
    }



    public static void sendFeedbackWithAttachment(String subject){

        Intent intent = new Intent(Intent.ACTION_SENDTO);

        intent.setData(Uri.parse("mailto:"));

        PackageManager packageManager = myActivity.getPackageManager();

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        if (resolveInfos.isEmpty()){

            Toast.makeText(myActivity, "No activity found!", Toast.LENGTH_SHORT).show();

        }else{
            // ACTION_SEND may be replied by some apps that are not email apps. However,
            // ACTION_SENDTO doesn't allow us to choose attachment. As a result, we use
            // an ACTION_SENDTO intent with email data to filter email apps and then send
            // email with attachment by ACTION_SEND.
            List<LabeledIntent> intents = new ArrayList<LabeledIntent>();

            for (ResolveInfo info : resolveInfos){

                Intent i = new Intent(Intent.ACTION_SENDTO);

                i.setPackage(info.activityInfo.packageName);

                i.setClassName(info.activityInfo.packageName, info.activityInfo.name);

                i.putExtra(Intent.EXTRA_EMAIL, new String[]{RECIPIENT});

                i.putExtra(Intent.EXTRA_SUBJECT, subject);

                intents.add(new LabeledIntent(i, info.activityInfo.packageName,
                        info.loadLabel(myActivity.getPackageManager()), info.icon));
            }

            Intent chooser = Intent.createChooser(intents.remove(0), "Error Report");

            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new LabeledIntent[intents.size()]));

            myActivity.startActivity(chooser);
        }
    }



    public static void sendErrorHttp(ArrayList<HashMap<String, String>> errorList){

        String log = "";

        for (int cnt = 0; cnt < errorList.size(); cnt++){

            log = log + "\n" + errorList.get(cnt).get("errInfo");
        }

        if (!log.isEmpty())

        {
            http.post(log, "https://sites.google.com/site/mydotscalendar/errors.txt");
        }
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



    private static boolean inDebugger(){

        //  If in Debugger Environment
        boolean debugging = Debug.isDebuggerConnected();

        return debugging;
    }



    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        mThread = thread;

        mException = exception;

        try{

            logCrash();

//            displayCrash(exception);

//            sendErrorMail();

        }catch (Exception ex){

            Log.e(getPackageName(myActivity), ex.getMessage());

        }finally{

            // Do I need to kill a process?
            // android.os.Process.killProcess(android.os.Process.myPid());

            // To get "unfortunately has stopped" popup
            // System.exit(1);

            // Needed to display any error messages.
            // System.exit(10);

            reportErrorHelper.reportEmptied();

            mThread = null;

            mException = null;

            mPrevExcept = "";

            if (oldHandler != null){

                oldHandler.uncaughtException(thread, exception);
            }
        }
    }



    public void onDestroy(){

        myActivity = null;
    }



    static class reportErrorHelper{

        private static final String LINE_SEPARATOR = "\n";

        // Not instantiated until an error and only once I think
        private static final StringBuilder mReportBuilder = new StringBuilder();

        private static String mError;

        private static String mExcept;

        private static String mCallStack;

        private static String mDeviceInfo;

        private static String mFirmware;



        // Empty the report as it is begin re-populated.
        private static void reportEmptied(){

            // No need to empty
            if (mReportBuilder.length() == 0){

                return;
            }

            mReportBuilder.setLength(0);

            mReportBuilder.trimToSize();
        }



        private static void clearReports(){

            // Clear all the variables
            mError = mExcept = mCallStack = mDeviceInfo = mFirmware = null;
        }



        private static String reportError(){

            if (mError != null){
                return mError;
            }

            mCrashInfo = new ApplicationErrorReport.CrashInfo(mException);

            if (mCrashInfo.exceptionMessage == null){

                mErrorMessage = "<unknown error>";
            }else{

                mErrorMessage = mCrashInfo.exceptionMessage
                        .replace(": " + mCrashInfo.exceptionClassName, "");
            }

            String throwFile = mCrashInfo.throwFileName == null ? "<unknown file>"
                    : mCrashInfo.throwFileName;

            mError = "\n************ " + mCrashInfo.exceptionClassName + " ************\n"
                    + mErrorMessage + LINE_SEPARATOR
                    + "\n File: " + throwFile
                    + "\n Method: " + mCrashInfo.throwMethodName + "()"
                    + "\n Line No.: " + Integer.toString(mCrashInfo.throwLineNumber)
                    + LINE_SEPARATOR;
            //			+ "Class: " + crashInfo.throwClassName + LINE_SEPARATOR

            return mError;

        }



        private static String reportException(){

            if (mExcept != null){
                return mExcept;
            }

            mExcept = "\n************ CAUSE OF ERROR ************\n"
                    + mException.toString()
                    + LINE_SEPARATOR;

            return mExcept;
        }



        private static String reportCallStack(){

            if (mCallStack != null){
                return mCallStack;
            }

            StringWriter stackTrace = new StringWriter();

            mException.printStackTrace(new PrintWriter(stackTrace));

            String callStack = stackTrace.toString();

            String errMsg = mException.toString();

            mCallStack = "\n************ CALLSTACK ************\n"
                    + callStack.replace(errMsg, "")
                    + LINE_SEPARATOR;

            return mCallStack;
        }



        private static String reportDeviceInfo(){

            if (mDeviceInfo != null){
                return mDeviceInfo;
            }

            mDeviceInfo = "\n************ DEVICE INFORMATION ***********\n"
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

            return mDeviceInfo;
        }



        private static String reportFirmware(){

            if (mFirmware != null){
                return mFirmware;
            }

            mFirmware = "\n************ FIRMWARE ************\n"
                    + "SDK: "
                    + Build.VERSION.SDK_INT
                    + LINE_SEPARATOR
                    + "Release: "
                    + Build.VERSION.RELEASE
                    + LINE_SEPARATOR
                    + "Incremental: "
                    + Build.VERSION.INCREMENTAL
                    + LINE_SEPARATOR;

            return mFirmware;
        }



        private static String errorMsg(String except){

            if (except.equals(mPrevExcept) && mReportBuilder.length() > 0){

                mReportBuilder.toString();
            }

            mPrevExcept = except;

            // Empty mReportBuilder
            reportEmptied();

            if (!except.contains("error")){

                mReportBuilder.append(reportError());
            }

            if (!except.contains("callstack")){

                mReportBuilder.append(reportCallStack());
            }

            if (!except.contains("deviceinfo")){

                mReportBuilder.append(reportDeviceInfo());
            }

            if (!except.contains("firmware")){

                mReportBuilder.append(reportFirmware());
            }

            return mReportBuilder.toString();
        }



        private static String errorMsg(){

            return errorMsg("");
        }
    }
}
