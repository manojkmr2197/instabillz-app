package com.app.billing.instabillz.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.UUID;

public class SingleTon {

    private static SingleTon singleTon;

    private SingleTon() {

    }

    public static final SingleTon getInstance() {

        if (singleTon == null) {
            singleTon = new SingleTon();
        }
        return singleTon;
    }


    public static final boolean isNetworkConnected(Activity activity) {
        ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static final boolean compareDateTime(OffsetDateTime offsetDateTime) {
        LocalDate date1 = offsetDateTime.toLocalDate();
        LocalDate date2 = OffsetDateTime.now().toLocalDate();

        // Compare times
        if (date1.isEqual(date2)) {
            return true;
        } else {
            return false;
        }
    }

    public static void hideKeyboard(Context context,Activity activity) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = activity.getCurrentFocus();
        if (imm != null && currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    // ✅ Returns epoch time of start of today (midnight)
    public static long getStartOfTodayEpoch() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis() / 1000; // to seconds
    }

    // ✅ Checks if given billing date falls within today
    public static boolean isSameDay(long billingDateEpoch, long todayStartEpoch) {
        long oneDaySeconds = 24 * 60 * 60;
        return billingDateEpoch >= todayStartEpoch && billingDateEpoch < todayStartEpoch + oneDaySeconds;
    }




    public static final String generateProductDocument(){
        return "PRODUCT-"+ UUID.randomUUID().toString();
    }

    public static final String generateStockDocument(){
        return "STOCK-"+ UUID.randomUUID().toString();
    }

    public static final String generateVendorDocument(){
        return "VENDOR-"+ UUID.randomUUID().toString();
    }

    public static final String generateExpenseDocument(){
        return "EXPENSE-"+ UUID.randomUUID().toString();
    }
    public static final String generateEmployeeDetailDocument(){
        return "EMPLOYEE-"+ UUID.randomUUID().toString();
    }

}
