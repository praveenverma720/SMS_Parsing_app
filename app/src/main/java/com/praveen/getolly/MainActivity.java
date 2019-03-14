package com.praveen.getolly;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private Activity activity;
    private RecyclerView listMessages;
    private RecyclerView.Adapter smsAdapter;
    private ImageButton createBtn,searchBtn,home_or_menu;
    private EditText searchTxt;
    private TextView title;
    private int clickedPos=-1;
    private ArrayList<SmsDetail> searchResults;
    private HashMap<String, ArrayList<SmsDetail>> groupById;
    private ArrayList<String> senderIds;
    private ArrayList<SmsDetail> lstSms;
    private String searchedText ="";
    private boolean searchedBtnClicked = false;
    private RecyclerView.LayoutManager layoutManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState!= null){
            clickedPos = savedInstanceState.getInt("clickedPos");
        }
        getActionBarToolbar();
        activity =this;
        listMessages = findViewById(R.id.listMessages);
        searchBtn =  findViewById(R.id.searchBtn);
        searchTxt =  findViewById(R.id.searchTxt);

        title =  findViewById(R.id.title);

        searchBtn.setOnClickListener(this);


        smsAdapter = new SmsAdapter();
        layoutManager = new LinearLayoutManager(activity);
        listMessages.setLayoutManager(layoutManager);
        senderIds = new ArrayList<>();
        searchResults = new ArrayList<>();
        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchedText = s.toString();
                searchedBtnClicked =false;
                smsAdapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        getPermission();

    }
    private void getPermission()
    {

        int permissionCheck1 = ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_SMS);
        if(permissionCheck1 == PackageManager.PERMISSION_DENIED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
        }
        if(PackageManager.PERMISSION_GRANTED == permissionCheck1)
        {
            loadMessageList();
        }
    }
    private  void loadMessageList()
    {
        HashMap<String, ArrayList<SmsDetail>> groupedSms =  groupSms(getAllSms());
        if(groupedSms!=null) {
            listMessages.setAdapter(smsAdapter);
            if(clickedPos!=-1 && clickedPos >=9)
                listMessages.scrollToPosition(clickedPos);
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {

            case R.id.searchBtn:
                searchTxt.setVisibility(View.VISIBLE);
                title.setVisibility(View.GONE);
                if(!searchedText.equalsIgnoreCase("")) {
                    searchResults.clear();
                    searchedBtnClicked = true;
                    for(int i=0;i<lstSms.size();i++)
                    {
                        if(lstSms.get(i).getMsg().toUpperCase().contains(searchedText.toUpperCase()))
                        {
                            searchResults.add(lstSms.get(i));
                        }
                    }
                    smsAdapter.notifyDataSetChanged();
                }
                break;


            default:
                break;
        }
    }

    public HashMap<String, ArrayList<SmsDetail>> groupSms(List<SmsDetail> listSms)
    {
        groupById = new HashMap<String, ArrayList<SmsDetail>>();
        for(int i=0;i<listSms.size();i++)
        {
            String currentId = listSms.get(i).getAddress();
            if (!groupById.containsKey(currentId)) {
                ArrayList<SmsDetail> list = new ArrayList<SmsDetail>();
                list.add(listSms.get(i));
                groupById.put(currentId, list);
                senderIds.add(currentId);
            } else {
                groupById.get(currentId).add(listSms.get(i));
            }
        }
        return groupById;
    }

    public List<SmsDetail> getAllSms() {
        lstSms = new ArrayList<SmsDetail>();
        SmsDetail objSms = new SmsDetail();
        Uri message = Uri.parse("content://sms");
        ContentResolver cr = activity.getContentResolver();
        Cursor c = cr.query(message, null, null, null, null);
        activity.startManagingCursor(c);
        int totalSMS = c.getCount();


        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {
                objSms = new SmsDetail();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }
                lstSms.add(objSms);
                c.moveToNext();





            }


        }
       // if (objSms.getMsg().contains("debit") || objSms.getMsg().contains("credit") ||  objSms.getMsg().contains("credited") || objSms.getMsg().contains("debited")){

       // }

        return lstSms;


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("clickedPos", clickedPos);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPermission();
        if(groupById!=null)
            groupById.clear();
        smsAdapter.notifyDataSetChanged();
        loadMessageList();
        if(searchedBtnClicked)
        {
            searchTxt.setVisibility(View.GONE);
            searchTxt.setText("");
            createBtn.setVisibility(View.VISIBLE);
            title.setVisibility(View.VISIBLE);
            home_or_menu.setVisibility(View.GONE);
            searchedBtnClicked = false;
        }
        smsAdapter.notifyDataSetChanged();



    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public class SmsAdapter extends RecyclerView.Adapter<SmsAdapter.ViewHolder>  {

        public  class ViewHolder extends RecyclerView.ViewHolder {
            public TextView senderId,senderCount,messageDetails,timeStamp,timeDate,blance;
            public LinearLayout lntlyt;
            public LinearLayout singlerow;

            public ViewHolder(View v) {
                super(v);
                singlerow =  v.findViewById(R.id.singlerow);
                senderId =  v.findViewById(R.id.senderId);
                senderCount = v.findViewById(R.id.senderCount);
                messageDetails =  v.findViewById(R.id.messageDetails);
                timeStamp =  v.findViewById(R.id.timeStamp);
                timeDate =  v.findViewById(R.id.timeDate);
                lntlyt =  v.findViewById(R.id.linlyt);
                blance = v.findViewById(R.id.blance);
            }
        }

        @Override
        public SmsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_sms, parent, false);
            ViewHolder vh = new ViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SmsAdapter.ViewHolder holder, final int position) {
            if(!searchedText.equalsIgnoreCase("") && searchedBtnClicked)
            {
                holder.senderId.setText(searchResults.get(position).getAddress());
                holder.timeStamp.setVisibility(View.GONE);
                holder.senderCount.setVisibility(View.GONE);
                holder.timeDate.setVisibility(View.VISIBLE);
                holder.lntlyt.setVisibility(View.GONE);
                holder.timeDate.setText(searchResults.get(position).getMsg());
            } else
            {
                holder.timeStamp.setVisibility(View.VISIBLE);
                holder.senderCount.setVisibility(View.VISIBLE);
                holder.lntlyt.setVisibility(View.VISIBLE);
                holder.timeDate.setVisibility(View.GONE);
                holder.senderId.setText(senderIds.get(position));
                holder.messageDetails.setText(groupById.get(senderIds.get(position)).get(0).getMsg());
                holder.senderCount.setText("" + groupById.get(senderIds.get(position)).size());
                String timeStamp= groupById.get(senderIds.get(position)).get(0).getTime();
                String date = getDate(Long.parseLong(timeStamp));
                String month = date.substring(3,5);
                String dd = date.substring(0,2);
                holder.timeStamp.setText(dd+" "+getMonth(month));
                holder.blance.setVisibility(View.VISIBLE);

                Pattern regEx = Pattern.compile("[iI][nN][rR]\\.?\\s*[,\\d]+\\.?\\d{0,2}");
                Matcher m= null;
                m = regEx.matcher(groupById.get(senderIds.get(position)).get(0).getMsg());

                if (m.find()) {
                    try {
                        holder.blance.setText("Current Amount: " + m.group(0));
                        Log.d("amount_value= ", "" + m.group(0));
                        String amount = (m.group(0).replaceAll("inr", ""));
                        //amount = amount.replaceAll("inr", "");

                        Log.d("matchedValue= ", "" + amount);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.d("No_matchedValue ", "No_matchedValue ");
                }


            }
            holder.singlerow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!searchedText.equalsIgnoreCase("") && searchedBtnClicked)
                    {

                        Intent i = new Intent(activity, ComposeSms.class);
                        i.putExtra("senderid",searchResults.get(position).getAddress());
                        i.putExtra("fromSearchText",true);
                        startActivity(i);
                    }
                    else
                    {
                        clickedPos = position;
                        Intent i = new Intent(activity, ComposeSms.class);
                        i.putExtra("senderid",senderIds.get(position));
                        i.putExtra("smsdata",groupById.get(senderIds.get(position)));
                        i.putExtra("position",position);
                        startActivity(i);
                    }


                }
            });
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return (!searchedText.equalsIgnoreCase("") && searchedBtnClicked)?searchResults.size():groupById.size();        }

        @Override
        public int getItemViewType(int position) {

            return position;
        }

    }
    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    private String getMonth(String month)
    {
        String smonth="";
        switch (month){
            case "01":
                smonth="Jan";
                break;
            case "02":
                smonth="Feb";
                break;
            case "03":
                smonth="Mar";
                break;
            case "04":
                smonth="Apr";
                break;
            case "05":
                smonth="May";
                break;
            case "06":
                smonth="Jun";
                break;
            case "07":
                smonth="Jul";
                break;
            case "08":
                smonth="Aug";
                break;
            case "09":
                smonth="Sep";
                break;
            case "10":
                smonth="Oct";
                break;
            case "11":
                smonth="Nov";
                break;
            case "12":
                smonth="Dec";
                break;
            default:
                break;
        }
        return smonth;
    }

    public Toolbar getActionBarToolbar() {
        if (toolbar == null) {
            toolbar = (Toolbar) findViewById(R.id.main_toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        return toolbar;
    }

}