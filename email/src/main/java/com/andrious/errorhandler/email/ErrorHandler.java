package com.andrious.errorhandler.email;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    private com.andrioussolutions.errorhandler.ErrorHandler mErrorHandler;

    private static Activity mActivity;

    private static String mEmailAddress = "<YOUR EMAIL ADDRESS>";




    public ErrorHandler(Activity activity, String emailAddress){

        mActivity = activity;

        mEmailAddress = emailAddress;

        mErrorHandler = com.andrioussolutions.errorhandler.ErrorHandler.getINSTANCE(activity);
    }




    public static void toCatch(Activity activity, String emailAddress){

        Thread.setDefaultUncaughtExceptionHandler(new ErrorHandler(activity, emailAddress));
    }




    @Override
    public void uncaughtException(Thread thread, Throwable exception){

        mErrorHandler.uncaughtException(thread, exception);

        String errorMsg = mErrorHandler.errorMsg("devicinfo firmware");

        sendErrorMail("ERROR!", errorMsg);
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

            mActivity.startActivity(sendIntent);
        }else{

            send = intents.add(sendIntent);
        }

        return send;
    }




    private static Intent intentErrorMail(String subject, String body){

        Intent sendIntent = new Intent(Intent.ACTION_SENDTO);

//        sendIntent.setData(Uri.parse("mailto:"));
//
//        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailAddress});

        sendIntent.setData(Uri.fromParts("mailto", mEmailAddress, null));

        sendIntent.setType("message/rfc822");

        sendIntent.putExtra(Intent.EXTRA_TEXT, body); //reportErrorHelper.errorMsg("DeviceInfo"));

        sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject.trim()); //"app_name crashed! Fix it!");

        return sendIntent;
    }




    public static void sendFeedbackWithAttachment(String subject){

        Intent intent = new Intent(Intent.ACTION_SENDTO);

        intent.setData(Uri.parse("mailto:"));

        PackageManager packageManager = mActivity.getPackageManager();

        List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(intent, 0);

        if (resolveInfos.isEmpty()){

            Toast.makeText(mActivity, "No activity found!", Toast.LENGTH_SHORT).show();

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

                i.putExtra(Intent.EXTRA_EMAIL, new String[]{mEmailAddress});

                i.putExtra(Intent.EXTRA_SUBJECT, subject);

                intents.add(new LabeledIntent(i, info.activityInfo.packageName,
                        info.loadLabel(mActivity.getPackageManager()), info.icon));
            }

            Intent chooser = Intent.createChooser(intents.remove(0), "Error Report");

            chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                    intents.toArray(new LabeledIntent[intents.size()]));

            mActivity.startActivity(chooser);
        }
    }


    public void onDestroy(){

        mActivity = null;

        mErrorHandler.onDestroy();

        mErrorHandler = null;
    }
}
