package com.example.rathana.retrofit_demo.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatHelper {

    public  static String formatDate(String date){
        Date d = new Date(date);
        SimpleDateFormat format=new SimpleDateFormat("dd/mm/yyyy");
        return format.format(d);

    }
}
