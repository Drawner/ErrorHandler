package com.andrious.errorhandler.display;

import android.app.Activity;
import android.content.Intent;

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

    private static com.andrious.errorhandler.display.ErrorHandler mErrorDisplay;

    private com.gtfp.errorhandler.ErrorHandler mErrorHandler;

    private static Thread mThread;

    private static Throwable mException;

    private Activity mActivity;




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

        if(mErrorDisplay == null){

            Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(activity));
        }
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        mThread = thread;

        mException = exception;

        String errorMsg = mErrorHandler.catchException(thread, exception);

        displayCrash(errorMsg);
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




    public static boolean inDebugger(){

        return com.gtfp.errorhandler.ErrorHandler.inDebugger();
    }




    private void displayCrash(String errorMsg){

        Intent intent = new Intent(mActivity, ExceptionDisplay.class);

        intent.putExtra("errorMsg", errorMsg);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        mActivity.startActivity(intent);

//        android.os.Process.killProcess(android.os.Process.myPid());
//
//        System.exit(10);
    }




    public void onDestroy(){

        mActivity = null;

        mErrorHandler.onDestroy();

        mErrorHandler = null;
    }
}