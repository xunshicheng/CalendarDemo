package com.cxs.demo.calendardemo;

import android.app.Activity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends Activity {

    private int index = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button createCalenderBtn = (Button) findViewById(R.id.calendar_account);
        createCalenderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initCalenderAccount();
            }
        });

        Button calenderAccountInfoBtn = (Button) findViewById(R.id.calendar_account_info);
        calenderAccountInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarAccount();
            }
        });

        Button calendarBtn = (Button) findViewById(R.id.calendar_view);
        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewCalender();
            }
        });

        Button insertBtn = (Button) findViewById(R.id.insert);
        insertBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                insertCalendarReminder();
            }
        });

        Button deleteBtn = (Button) findViewById(R.id.delete);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteEvents();
            }
        });
    }

    /**
     * 创建日历帐号
     */
    private void initCalenderAccount() {
        TimeZone timeZone = TimeZone.getDefault();
        ContentValues value = new ContentValues();
        value.put(CalendarContract.Calendars.NAME, "yy");

        value.put(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange");
        value.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, "mytt");
        value.put(CalendarContract.Calendars.VISIBLE, 1);
        value.put(CalendarContract.Calendars.CALENDAR_COLOR, -9206951);
        value.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER);
        value.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        value.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, timeZone.getID());
        value.put(CalendarContract.Calendars.OWNER_ACCOUNT, "mygmailaddress@gmail.com");
        value.put(CalendarContract.Calendars.CAN_ORGANIZER_RESPOND, 0);

        Uri calendarUri = CalendarContract.Calendars.CONTENT_URI;
        calendarUri = calendarUri.buildUpon()
                .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, "mygmailaddress@gmail.com")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, "com.android.exchange")
                .build();

        getContentResolver().insert(calendarUri, value);
    }

    /**
     * 显示帐号信息
     */
    private void showCalendarAccount() {
        try {
            Cursor userCursor = getContentResolver().query(CalendarContract.Calendars.CONTENT_URI, null, null, null, null);

            Log.i("cxs", "===== Count: " + userCursor.getCount());
            Toast.makeText(this, "Count: " + userCursor.getCount(), Toast.LENGTH_LONG).show();

            for (userCursor.moveToFirst(); !userCursor.isAfterLast(); userCursor.moveToNext()) {
                System.out.println("name: " + userCursor.getString(userCursor.getColumnIndex("ACCOUNT_NAME")));

                String userName1 = userCursor.getString(userCursor.getColumnIndex("name"));
                String userName0 = userCursor.getString(userCursor.getColumnIndex("ACCOUNT_NAME"));
                Log.i("cxs", "===== NAME: " + userName1 + " -- ACCOUNT_NAME: " + userName0);
                Toast.makeText(this, "NAME: " + userName1 + " -- ACCOUNT_NAME: " + userName0, Toast.LENGTH_LONG).show();
            }
        } catch (SecurityException e) {
            Log.i("cxs", "========= calendarAccount: " + e);
        }
    }

    /**
     * 跳转到系统日历界面
     */
    private void startCalendarActivity() {
        //打开日历来插入事件，vivo x9 报ActivityNotFoundException
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 5, 6, 17, 15);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 5, 6, 17, 30);
        Intent intent = new Intent(Intent.ACTION_EDIT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "testest" + index)
                .putExtra(CalendarContract.Events.CALENDAR_ID, 1)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    private void viewCalender() {
        //只是打开日历进行查看
        long startMillis = System.currentTimeMillis();
        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
        startActivity(intent);
    }

    /**
     * 在后台往系统日历插入事件提醒
     */
    private void insertCalendarReminder() {
        //先定义一个URL，到时作为调用系统日历的uri的参数

        long calID = 1;
        long startMillis = 0;
        long endMillis = 0;
        Calendar beginTime = Calendar.getInstance();
        beginTime.set(2017, 5, 6, 17, 30);  //注意，月份的下标是从0开始的
        startMillis = beginTime.getTimeInMillis();  //插入日历时要取毫秒计时
        Calendar endTime = Calendar.getInstance();
        endTime.set(2017, 5, 6, 17, 50);
        endMillis = endTime.getTimeInMillis();

        ContentValues eValues = new ContentValues();  //插入事件
        ContentValues rValues = new ContentValues();  //插入提醒，与事件配合起来才有效
        TimeZone tz = TimeZone.getDefault();//获取默认时区

        //插入日程
        eValues.put(CalendarContract.Events.DTSTART, startMillis);
        eValues.put(CalendarContract.Events.DTEND, endMillis);
        eValues.put(CalendarContract.Events.TITLE, "见导师");
        eValues.put(CalendarContract.Events.DESCRIPTION, "去实验室见研究生导师");

        eValues.put(CalendarContract.Events.CALENDAR_ID, calID); ///////添加事件时必须有该参数，且对应的Calendar帐号存在

        eValues.put(CalendarContract.Events.EVENT_LOCATION, "计算机学院");
        eValues.put(CalendarContract.Events.EVENT_TIMEZONE, tz.getID());
        try {
            Uri uri = getContentResolver().insert(CalendarContract.Events.CONTENT_URI, eValues);
            //插完日程之后必须再插入以下代码段才能实现提醒功能
            Log.i("cxs", "----------" + uri);
            String myEventsId = uri.getLastPathSegment(); // 得到当前表的_id
            rValues.put(CalendarContract.Reminders.EVENT_ID, myEventsId);
            rValues.put(CalendarContract.Reminders.MINUTES, 10); //提前10分钟提醒
            rValues.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);   //如果需要有提醒,必须要有这一行
            Uri reminderUri = CalendarContract.Reminders.CONTENT_URI;
            getContentResolver().insert(reminderUri, rValues);
        } catch (SecurityException e) {
            Log.i("cxs", "========== insert " + e);
        } catch (Exception e) {
            Log.i("cxs", "======== exception " + e);
        }

    }

    /**
     * 删除日历事件
     */
    private void deleteEvents() {
        try {
            int rows = getContentResolver().delete(CalendarContract.Events.CONTENT_URI,
                    CalendarContract.Events.DESCRIPTION+"=?",
                    new String[]{"去实验室见研究生导师"});
            Log.i("cxs", "---------- row: " + rows);
        } catch (SecurityException e) {
            Log.i("cxs", "======== delete " + e);
        }
    }
}
