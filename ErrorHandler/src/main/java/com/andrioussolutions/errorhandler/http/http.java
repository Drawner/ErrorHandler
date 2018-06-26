package com.andrioussolutions.errorhandler.http;

import com.andrioussolutions.errorhandler.ErrorHandler;

import android.os.AsyncTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


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
public class http{

    private static final String ERROR_TAG = "http";

    private http(){}


    public static void post(String postData, String urlString){

        new postAsync().execute(postData, urlString);
    }


    private static class postAsync extends AsyncTask<String, Void, Boolean>{

        /**
         * The system calls this to perform work in a worker thread and
         * delivers it the parameters given to AsyncTask.execute()
         */
        @Override
        protected Boolean doInBackground(String... urls){

            return postError(urls[0], urls[1]);
        }

        /**
         * The system calls this to perform work in the UI thread and delivers
         * the result from doInBackground()
         */
        @Override
        protected void onPostExecute(Boolean result){

            if (result){

            }
        }
    }


    private static boolean postError(String postData, String urlString){

        boolean posted = true;
        String data;

        try{

            data = http.getData(postData, urlString);

            posted = !data.isEmpty();

        }catch (Exception ex){

            posted = false;

            ErrorHandler.logError(ex);

        }finally{

            return posted;
        }
    }


    private static String getData(String postData, String urlString) throws IOException{

        StringBuilder respData = new StringBuilder();

        URL url = new URL(urlString);

        URLConnection conn = url.openConnection();

        HttpURLConnection httpConn = (HttpURLConnection) conn;

        httpConn.setUseCaches(false);

        httpConn.setRequestProperty("User-Agent", "YourApp");

        httpConn.setConnectTimeout(30000);

        httpConn.setReadTimeout(30000);

        httpConn.setRequestMethod("POST");

        httpConn.setDoOutput(true);

        OutputStream os = conn.getOutputStream();

        InputStream postStream = toInputStream(postData, "UTF-8");

        copy(postStream, os);

        postStream.close();

        os.flush();

        os.close();

        httpConn.connect();

        try{

            int responseCode = httpConn.getResponseCode();

            if (responseCode >= HttpURLConnection.HTTP_OK
                    && responseCode < HttpURLConnection.HTTP_NOT_FOUND){

                InputStream is = httpConn.getInputStream();

                InputStreamReader isr;

                isr = new InputStreamReader(is);

                char[] buffer = new char[1024];

                int len;

                while ((len = isr.read(buffer)) != -1){

                    respData.append(buffer, 0, len);
                }

                if (isr != null){

                    isr.close();
                }

                is.close();

                httpConn.disconnect();
            }else{

                throw new IOException(httpConn.getResponseCode() + " " + httpConn.getResponseMessage());

//            BufferedReader errorReader = new BufferedReader(
//                    new InputStreamReader(httpConn.getErrorStream()));
//
//            StringBuilder builder = new StringBuilder();
//
//            String output;
//
//            while ((output = errorReader.readLine()) != null){
//
//                builder.append(output + "\n");
//            }
//
//            errorReader.close();
//
//            output = builder.toString();
//
//            Log.e(ERROR_TAG, output);
//
//            httpConn.disconnect();
            }

        }catch (IOException ex){

            httpConn.disconnect();

            throw ex;
        }

        return respData.toString();
    }


    private static InputStream toInputStream(String input, String encoding) throws IOException{

        byte[] bytes = encoding != null ? input.getBytes(encoding) : input.getBytes();

        return new ByteArrayInputStream(bytes);
    }

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;


    private static long copyLarge(InputStream input, OutputStream output)
            throws IOException{
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        long count = 0;

        int n;

        while (-1 != (n = input.read(buffer))){

            output.write(buffer, 0, n);

            count += n;
        }

        return count;
    }


    private static int copy(InputStream input, OutputStream output) throws IOException{

        long count = copyLarge(input, output);

        if (count > Integer.MAX_VALUE){

            return -1;
        }

        return (int) count;
    }
}