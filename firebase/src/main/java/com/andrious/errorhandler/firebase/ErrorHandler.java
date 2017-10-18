package com.andrious.errorhandler.firebase;

import com.google.firebase.crash.FirebaseCrash;

import android.app.Activity;
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

        mErrorHandler = com.gtfp.errorhandler.ErrorHandler.getINSTANCE(activity);
    }




    public static void toCatch(Activity activity){

        Thread.setDefaultUncaughtExceptionHandler(getINSTANCE(activity));
    }




    public static com.andrious.errorhandler.firebase.ErrorHandler getINSTANCE(Activity activity){

        if (mErrorFirebase == null){

            mErrorFirebase = new ErrorHandler(activity);
        }

        return mErrorFirebase;
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




    public void defaultExceptionHandler(Thread thread, Throwable exception){

        com.gtfp.errorhandler.ErrorHandler.defaultExceptionHandler(thread, exception);
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        if (com.gtfp.errorhandler.ErrorHandler.inDebugger()){

            mErrorHandler.uncaughtException(thread, exception);
        }else{

            FirebaseCrash.report(exception);
        }

        defaultExceptionHandler(thread, exception);
    }




    public void onDestroy(){

        mErrorFirebase = null;

        mErrorHandler.onDestroy();

        mErrorHandler = null;
    }




    private static com.andrious.errorhandler.firebase.ErrorHandler mErrorFirebase;

    private com.gtfp.errorhandler.ErrorHandler mErrorHandler;
}
