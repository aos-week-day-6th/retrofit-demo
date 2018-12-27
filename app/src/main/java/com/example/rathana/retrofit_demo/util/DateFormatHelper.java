package com.example.rathana.retrofit_demo.util;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatHelper {

    public  static String formatDate(String date){
        Timestamp timestamp=Timestamp.valueOf(date);
        //SimpleDateFormat format=new SimpleDateFormat("%tm/%td/%ty");
        return String.format("%tm/%td/%ty %tH:%tM:%tS",timestamp);

    }
}
