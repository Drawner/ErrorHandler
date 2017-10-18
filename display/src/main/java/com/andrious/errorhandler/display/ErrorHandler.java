package com.andrious.errorhandler.display;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

/**
 * Copyright (C) 2017  Andrious Solutions Ltd.
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
 * Created  07 Oct 2017
 */
public class ErrorHandler implements
        java.lang.Thread.UncaughtExceptionHandler{

    private ErrorHandler(Activity activity){

        mActivity = activity;

        mErrorHandler = com.gtfp.errorhandler.ErrorHandler.getINSTANCE(activity);
    }




    public static com.andrious.errorhandler.display.ErrorHandler getINSTANCE(Activity activity){

        if (mErrorDisplay == null){

            mErrorDisplay = new ErrorHandler(activity);
        }

        return mErrorDisplay;
    }




    public static void toCatch(Activity activity){

        if (mErrorDisplay == null){

            Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(activity));
        }
    }




    public static void defaultExceptionHandler(){

        if (mThread != null){

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




    public void catchException(Thread thread, Throwable exception){

        uncaughtException(thread, exception);
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        mThread = thread;

        mException = exception;

        String errorMsg = mErrorHandler.catchException(thread, exception);

        final Intent intent = new Intent(mActivity, ErrorDisplay.class);
//
//        final Intent intent = new Intent ();
//
//        intent.setAction (".ErrorHandler$ErrorDisplay");

        intent.putExtra("errorMsg", errorMsg);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

//        new Thread(){
//
//            public void run(){
//
////                mActivity.startActivity(intent);
//                mActivity.getApplicationContext().startActivity(intent);
//            }
//        }.start();

//        mActivity.getApplicationContext().startActivity(intent);
//
//        android.os.Process.killProcess(android.os.Process.myPid());
//
//        System.exit(10);


//        if(isUIThread()) {
//
//            mActivity.getApplicationContext().startActivity(intent);
//
//        }else{  //handle non UI thread throw uncaught exception
//
//            new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//                @Override
//                public void run() {
//                    mActivity.getApplicationContext().startActivity(intent);
//                }
//            });
//        }

//        new Handler(Looper.getMainLooper()).post(new Runnable() {
//
//            @Override
//            public void run() {
//
//                mActivity.getApplicationContext().startActivity(intent);
//            }
//        });


//        new Thread(){
//
//            @Override
//            public void run(){
//                Looper.prepare();
//
//                new Handler().post(new Runnable() {
//
//                @Override
//                public void run() {
//
//                    mActivity.getApplicationContext().startActivity(intent);
//                }
//            });
//                Looper.loop();
//                Looper.myLooper().quit();
//            }
//        }.start();



        new Handler().post(new Runnable() {

            @Override
            public void run() {

                mActivity.getApplicationContext().startActivity(intent);
            }
        });

    }




    // If current thread is an UI thread.
    public boolean isUIThread(){

        return Looper.getMainLooper().getThread() == Thread.currentThread();
    }


    public void onDestroy(){

        mActivity = null;

        mErrorHandler.onDestroy();

        mErrorHandler = null;
    }




//    public class ErrorDisplay extends Activity{
//
//        @Override
//        protected void onCreate(Bundle savedInstanceState){
//            super.onCreate(savedInstanceState);
//
//            final AlertDialog alertDialog;
//
//            alertDialog = new AlertDialog.Builder(mActivity).create();
//
//            alertDialog.setTitle("Error");
//
//            alertDialog.setMessage(mActivity.getIntent().getStringExtra("errorMsg"));
//
//            alertDialog.setMessage(com.gtfp.errorhandler.ErrorHandler.getErrorMessage());
//
//            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){
//
//                public void onClick(DialogInterface dialog, int id){
//
//                    alertDialog.dismiss();
//
//                    android.os.Process.killProcess(android.os.Process.myPid());
//
//                    System.exit(10);
//                }
//            };
//
//            alertDialog.setCancelable(false);
//
//            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);
//
//            alertDialog.show();
//        }
//    }



    private static com.andrious.errorhandler.display.ErrorHandler mErrorDisplay;

    private static Thread mThread;

    private static Throwable mException;

    private com.gtfp.errorhandler.ErrorHandler mErrorHandler;

    private Activity mActivity;

}
