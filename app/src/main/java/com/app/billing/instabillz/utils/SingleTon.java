package com.app.billing.instabillz.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Pair;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Calendar;
import java.util.Date;
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

    public static final String generateCategoryDocument(){
        return "CATEGORY-"+ UUID.randomUUID().toString();
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

    public static Pair<Date, Date> getStartAndEndDate(String rangeType) {
        Calendar calendar = Calendar.getInstance();
        Date startDate;
        Date endDate;

        switch (rangeType.toLowerCase()) {
            case "today":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                startDate = calendar.getTime();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;

            case "yesterday":
                calendar.add(Calendar.DATE, -1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                endDate = calendar.getTime();
                break;

            case "this week":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();

                Calendar endOfWeek = Calendar.getInstance();
                endOfWeek.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek() + 6);
                endOfWeek.set(Calendar.HOUR_OF_DAY, 23);
                endOfWeek.set(Calendar.MINUTE, 59);
                endOfWeek.set(Calendar.SECOND, 59);
                endDate = endOfWeek.getTime();
                break;

            case "this month":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();

                Calendar endOfMonth = Calendar.getInstance();
                endOfMonth.set(Calendar.DAY_OF_MONTH, endOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH));
                endOfMonth.set(Calendar.HOUR_OF_DAY, 23);
                endOfMonth.set(Calendar.MINUTE, 59);
                endOfMonth.set(Calendar.SECOND, 59);
                endDate = endOfMonth.getTime();
                break;

            case "last 3 months":
                calendar.add(Calendar.MONTH, -2);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();

                Calendar end3 = Calendar.getInstance();
                end3.set(Calendar.HOUR_OF_DAY, 23);
                end3.set(Calendar.MINUTE, 59);
                end3.set(Calendar.SECOND, 59);
                endDate = end3.getTime();
                break;

            case "last 6 months":
                calendar.add(Calendar.MONTH, -5);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();

                Calendar end6 = Calendar.getInstance();
                end6.set(Calendar.HOUR_OF_DAY, 23);
                end6.set(Calendar.MINUTE, 59);
                end6.set(Calendar.SECOND, 59);
                endDate = end6.getTime();
                break;

            default:
                // Default → today
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                startDate = calendar.getTime();
                endDate = new Date();
        }

        return new Pair<>(startDate, endDate);
    }

    // ✅ Convert Date range → Epoch (milliseconds)
    public static Pair<Long, Long> getEpochStartAndEnd(String rangeType) {
        // 2️⃣ Date range filter
        long startEpoch = 0L;
        long endEpoch = Long.MAX_VALUE;
        ZoneOffset istOffset = ZoneOffset.ofHoursMinutes(5, 30);
        OffsetDateTime now = OffsetDateTime.now();

        switch (rangeType) {
            case "Today":
                startEpoch = now.toLocalDate().atStartOfDay().toEpochSecond(istOffset);
                endEpoch = now.toLocalDate().atTime(23, 59, 59).toEpochSecond(istOffset);
                break;

            case "Yesterday":
                OffsetDateTime yesterday = now.minusDays(1);
                startEpoch = yesterday.toLocalDate().atStartOfDay().toEpochSecond(istOffset);
                endEpoch = yesterday.toLocalDate().atTime(23, 59, 59).toEpochSecond(istOffset);
                break;

            case "This Week":
                OffsetDateTime weekStart = now.with(DayOfWeek.MONDAY).toLocalDate().atStartOfDay().atOffset(istOffset);
                OffsetDateTime weekEnd = now.with(DayOfWeek.SUNDAY).toLocalDate().atTime(23, 59, 59).atOffset(istOffset);
                startEpoch = weekStart.toEpochSecond();
                endEpoch = weekEnd.toEpochSecond();
                break;

            case "This Month":
                OffsetDateTime monthStart = now.withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                OffsetDateTime monthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).toLocalDate().atTime(23, 59, 59).atOffset(istOffset);
                startEpoch = monthStart.toEpochSecond();
                endEpoch = monthEnd.toEpochSecond();
                break;

            case "Last 3 Months":
                OffsetDateTime last3Months = now.minusMonths(3).withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                startEpoch = last3Months.toEpochSecond();
                endEpoch = now.toEpochSecond();
                break;

            case "Last 6 Months":
                OffsetDateTime last6Months = now.minusMonths(6).withDayOfMonth(1).toLocalDate().atStartOfDay().atOffset(istOffset);
                startEpoch = last6Months.toEpochSecond();
                endEpoch = now.toEpochSecond();
                break;
        }
        return new Pair<>(startEpoch, endEpoch);
    }


}
