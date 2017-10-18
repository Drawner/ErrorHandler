package com.andrious.errorhandler.display;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

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
 * Created  17 Oct 2017
 */
public class ErrorDisplay extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        final AlertDialog alertDialog;

        alertDialog = new AlertDialog.Builder(this).create();

        alertDialog.setTitle("Error");

        alertDialog.setMessage(com.gtfp.errorhandler.ErrorHandler.getErrorMessage());

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener(){

            public void onClick(DialogInterface dialog, int id){

                alertDialog.dismiss();

                android.os.Process.killProcess(android.os.Process.myPid());

                System.exit(10);
            }
        };

        alertDialog.setCancelable(false);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", listener);

        alertDialog.show();
    }
}
