package com.gtfp.errorhandler;

import com.gtfp.errorhandler.db.dbRecError;
import com.gtfp.errorhandler.frmwrk.db.dbHelper;

import android.app.Fragment;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;


public class CrashFragment extends Fragment{

    private TextView error;

    private boolean mErrorList;

    private ArrayList<HashMap<String, String>> mListOfErrors;

    private EditText  mErrInfo;

    private ListView mListView;

    private dbHelper mDBErrDatabase;

    private LayoutInflater mLayoutInflater;

    private int mCurrPosn = 0;

    private fragArguments mArgs;


    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mArgs = new fragArguments(getArguments());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){

        View fragView = null;

        try{

            fragView = ListAppErrors(inflater, container, savedInstanceState);

            fragView.setBackgroundColor(Color.BLACK);

            fragView.setClickable(true);

        }catch (Exception ex){

            ErrorHandler.logError(ex);

            getActivity().getFragmentManager().popBackStack();
        }

        return fragView;
    }


    private View ListAppErrors(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState){

        mLayoutInflater = inflater;

        mErrorList = mArgs.getArgument("ERROR_LIST", false);

        View rootView;

        if (mErrorList){

            rootView = inflater.inflate(R.layout.error_list, container, false);

            mErrInfo = (EditText) rootView.findViewById(R.id.errInfo);

            mErrInfo.setVerticalScrollBarEnabled(true);

            mErrInfo.setCursorVisible(false);

            mErrInfo.setFocusable(false);

            mListView = (ListView) rootView.findViewById(R.id.listView);

            Button clear, send;

            clear = (Button) rootView.findViewById(R.id.clear);

            clear.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                }
            });

            send = (Button) rootView.findViewById(R.id.send);

            send.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){

                    new ErrorEmail().send();
                }
            });

            mDBErrDatabase = dbRecError.getDbHelper(getActivity());

            if (mDBErrDatabase != null){

                mListOfErrors = mDBErrDatabase.listRecs();
            }

            mListView.setAdapter(new ErrorsList(mListView));
        }else{

            rootView = inflater.inflate(R.layout.activity_crash, container, false);

            error = (TextView) rootView.findViewById(R.id.error_message);

            error.setText(mArgs.getArgument("error", ""));
        }

        return rootView;
    }


    @Override
    public void onStop(){
        super.onStop();

        if (mDBErrDatabase != null){

            // close the db connection...
            mDBErrDatabase.close();
        }
    }

    @Override
    public void onResume(){
        super.onResume();

        if (mDBErrDatabase != null){

            mDBErrDatabase.open();
        }
    }


    @Override
    public void onDestroy(){
        super.onDestroy();

        mArgs = null;

        mListOfErrors = null;

        mLayoutInflater = null;

        if (mDBErrDatabase != null){

            mDBErrDatabase.onDestroy();

            mDBErrDatabase = null;
        }
    }


    private class fragArguments{

        private Bundle mArgs;


        fragArguments(Bundle args){

            mArgs = args;
        }


        String getArgument(String key, String defValue){

            if (mArgs != null){

                defValue = mArgs.getString(key, defValue);
            }

            return defValue;
        }


        Boolean getArgument(String key, boolean defValue){

            if (mArgs != null){

                defValue = mArgs.getBoolean(key, defValue);
            }

            return defValue;
        }
    }


    public class ErrorsList extends BaseAdapter{

        private int mCount = 0;

        private Object mObj = new Object();

        private TextView mErrMsg;

        private TextView mSelectedRow;

        private Drawable mBackground;

        private ColorStateList mTextColours;


        public ErrorsList(ListView errList){

            mCount = mListOfErrors.size();

            // One click will edit that selected item.
            errList.setOnItemClickListener(new AdapterView.OnItemClickListener(){

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long rowId){

                    if (mCurrPosn != position){

                        selectRow(view, position);
                    }
                }
            });

        }

        @Override
        // This is being called alot by the UI thread.
        public int getCount(){

            return mCount;
        }


        @Override
        // This should be called recycleView()  hehe!
        public View getView(int position, View convertView, ViewGroup parent){

            if (convertView != null){

                mErrMsg = (TextView) convertView.getTag();
            }else{

                convertView = mLayoutInflater.inflate(R.layout.errrow, parent, false);

                mErrMsg = (TextView) convertView.findViewById(R.id.errMsg);

                convertView.setTag(mErrMsg);

                convertView.setBackgroundResource(R.drawable.rounded_corners);

                if (position == 0){

                    mBackground = mErrMsg.getBackground();

                    mTextColours = mErrMsg.getTextColors();

                    mSelectedRow = mErrMsg;
                }
            }

            mErrMsg.setText(mListOfErrors.get(position).get("errMsg"));

            if (mCurrPosn == position){

                selectRow(convertView, position);
            }else{

                if (mErrMsg == mSelectedRow){

                    mErrMsg.setTextColor(mTextColours);

                    mErrMsg.setBackground(mBackground);
                }
            }

            return convertView;
        }


        private void selectRow(View view, int position){

            mSelectedRow.setTextColor(mTextColours);

            mSelectedRow.setBackground(mBackground);

            mErrInfo.setText(mListOfErrors.get(position).get("errInfo"));

            mErrMsg = (TextView) view.getTag();

            mErrMsg.setTextColor(Color.BLACK);

            mErrMsg.setBackgroundColor(Color.WHITE);

            mSelectedRow = mErrMsg;

            mCurrPosn = position;
        }


        @Override
        // Return the rowid
        public long getItemId(int index){

            return 1L;
        }


        @Override
        public Object getItem(int index){

            return mObj;
        }
    }

    public class ErrorEmail{

        public boolean send(){

            boolean send = true;

            try{

                ErrorHandler.sendErrorMail(mListOfErrors);

//                ErrorHandler.sendErrorHttp(mListOfErrors);

            }catch (Exception ex){

                ErrorHandler.logError(ex);

                send = false;
            }

            return send;
        }
    }
}
