package com.webcommander.util

import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat

/**
 * Created by zobair on 30/01/2015.
 */
class DateUtil {
    static final String[] MONTHS_FULL_LOWER = ['january', 'february', 'march', 'april', 'may', 'june', 'july', 'august', 'september', 'october', 'november', 'december']


    static final SimpleDateFormat indexerDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    static final String DATE_FORMAT_dd_MM_yyyy_HH_mm_ss = "dd-MM-yyyy HH:mm:ss";
    static final String DATE_FORMAT_dd_MM_yyyy = "dd-MM-yyyy";

    // Q1: Jan,Feb,Mar
    // Q2: Apr,May,June
    // Q3: July, Aug, Sept
    // Q4: Oct,Nov,Dec
    static final int[] quarters = [ 1, 1, 1, 2, 2, 2, 3, 3, 3, 4, 4, 4 ];

    static int getLastYear() {

        Calendar now = Calendar.getInstance();

        int year = now.get(Calendar.YEAR);

        year = year - 1;

        return year;

    }

    static int getYear(String date) throws ParseException {

        java.util.Date givenDate = indexerDateFormat.parse(date);

        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTime(givenDate);

        int year = curCalendar.get(Calendar.YEAR);

        return year;

    }

    static int getLastWeekNumber() {

        Calendar now = Calendar.getInstance();

        now.add(Calendar.WEEK_OF_YEAR, -1);
        int weeknumber = now.get(Calendar.WEEK_OF_YEAR);

        return weeknumber;

    }

    static int getLastMonth() {

        Calendar now = Calendar.getInstance();

        now.add(Calendar.MONTH, -1);
        int month = now.get(Calendar.MONTH);

        return month;

    }

    static int getLastQuarterNumber() {

        Calendar now = Calendar.getInstance();

        int thisMonth = now.get(Calendar.MONTH);
        int thisQuarter = quarters[thisMonth];

        if (thisQuarter == 1)
            thisQuarter = 4;
        else
            thisQuarter = thisQuarter - 1;

        return thisQuarter;

    }

    static int getQuarterNumber(String date) throws ParseException {

        java.util.Date givenDate = indexerDateFormat.parse(date);

        Calendar curCalendar = Calendar.getInstance();
        curCalendar.setTime(givenDate);

        int thisMonth = curCalendar.get(Calendar.MONTH);
        int thisQuarter = quarters[thisMonth];

        return thisQuarter;

    }

    static int getWeekNumber(String date) throws ParseException {

        java.util.Date givenDate = indexerDateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(givenDate);

        int weeknumber = cal.get(Calendar.WEEK_OF_YEAR);

        return weeknumber;

    }

    static int getMonthNumber(String date) throws ParseException {

        java.util.Date givenDate = indexerDateFormat.parse(date);

        Calendar cal = Calendar.getInstance();
        cal.setTime(givenDate);

        int weeknumber = cal.get(Calendar.MONTH);

        return weeknumber;

    }

    static boolean equalsToday(String date) {

        return equalsToday(date, indexerDateFormat);

    }

    static boolean equalsToday(String date, SimpleDateFormat parseFormat) {

        if (!date) {
            return false;
        }

        java.util.Date givenDate = null;

        try {
            givenDate = parseFormat.parse(date);
        } catch (Exception e) {
            return false;
        }

        if (!givenDate) {
            return false;
        }

        Calendar now = Calendar.getInstance();

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean isEquals = (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.MONTH) == timeToCheck.get(Calendar.MONTH));

        return isEquals;

    }

    static boolean equalsToday(Date givenDate) {

        if (givenDate == null)
            return false;

        Calendar now = Calendar.getInstance();

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean isEquals = (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.MONTH) == timeToCheck.get(Calendar.MONTH));

        return isEquals;

    }

    static boolean equalsYesterday(String date) {

        return equalsYesterday(date, indexerDateFormat);

    }

    static boolean equalsYesterday(String date, SimpleDateFormat parseFormat) {

        java.util.Date givenDate = null;

        try {
            givenDate = parseFormat.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -1);

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean isEquals = (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.MONTH) == timeToCheck.get(Calendar.MONTH));

        return isEquals;

    }

    static boolean equalsYesterday(Date givenDate) {

        if (givenDate == null)
            return false;

        Calendar now = Calendar.getInstance();
        now.add(Calendar.DATE, -1);

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean isEquals = (now.get(Calendar.YEAR) == timeToCheck.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == timeToCheck.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.MONTH) == timeToCheck.get(Calendar.MONTH));

        return isEquals;

    }



    static boolean containsThisWeek(Date givenDate) {

        if (givenDate == null)
            return false;

        Calendar now = Calendar.getInstance();

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean isEquals = (now.get(Calendar.WEEK_OF_YEAR) == timeToCheck.get(Calendar.WEEK_OF_YEAR));

        return isEquals;

    }

    static boolean containsThisWeekByDay(Date givenDate) {

        if (givenDate == null)
            return false;

        Calendar thisWeek = Calendar.getInstance();
        thisWeek.add(Calendar.DATE, -7);

        Calendar timeToCheck = Calendar.getInstance();
        timeToCheck.setTime(givenDate);

        boolean containThisWeek = timeToCheck.after(thisWeek);

        return containThisWeek;

    }

    static Date getLastWeekStartDate() {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, 1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

    static Date getLastWeekEndDate() {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.WEEK_OF_YEAR, -1);
        cal.set(Calendar.DAY_OF_WEEK, 7);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();

    }

    static boolean betweenLastWeek(String date) {

        java.util.Date givenDate = null;

        try {
            givenDate = indexerDateFormat.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (givenDate != null && givenDate.after(getLastWeekStartDate()) && givenDate.before(getLastWeekEndDate())) {

            return true;
        }

        return false;

    }

    static Date getLastMonthStartDate() {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DAY_OF_MONTH, 1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

    static Date getLastMonthEndDate() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DAY_OF_MONTH, 1); // set the date to 1st of the current
        // month

        cal.add(Calendar.DATE, -1); // date -1 to get the last date of previous
        // month

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();

    }

    static boolean betweenLastMonth(String date) {

        java.util.Date givenDate = null;

        try {
            givenDate = indexerDateFormat.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (givenDate != null && givenDate.after(getLastMonthStartDate()) && givenDate.before(getLastMonthEndDate())) {

            return true;
        }

        return false;

    }

    static Date getLastYearStartDate() {

        Calendar cal = Calendar.getInstance();

        cal.add(Calendar.YEAR, -1);
        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1);

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

    static Date getLastYearEndDate() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.MONTH, Calendar.JANUARY);
        cal.set(Calendar.DATE, 1); // set jan 01 of current year

        cal.add(Calendar.DATE, -1); // date -1 to get the last date of previous
        // month

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();

    }

    static boolean betweenLastYear(String date) {

        java.util.Date givenDate = null;

        try {
            givenDate = indexerDateFormat.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (givenDate != null && givenDate.after(getLastYearStartDate()) && givenDate.before(getLastYearEndDate())) {

            return true;
        }

        return false;

    }

    static Date getLastQuarterStartDate() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.DATE, 1);

        int thisMonth = cal.get(Calendar.MONTH);
        int thisQuarter = quarters[thisMonth];

        if (thisQuarter == 1) { // means jan,feb,march then set last quarter
            // start date to October 1st of previous year
            cal.add(Calendar.YEAR, -1);
            cal.set(Calendar.MONTH, Calendar.OCTOBER);
        } else if (thisQuarter == 2) { // means April, May, June then set last
            // quarter start date to January 1st
            cal.set(Calendar.MONTH, Calendar.JANUARY);
        } else if (thisQuarter == 3) { // means July, August, September then set
            // last quarter start date to April 1st
            cal.set(Calendar.MONTH, Calendar.APRIL);
        } else if (thisQuarter == 4) { // means Oct,nov,dec then set last
            // quarter start date to July 1st
            cal.set(Calendar.MONTH, Calendar.JULY);
        }

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

    static Date getLastQuarterEndDate() {

        Calendar cal = Calendar.getInstance();

        Date lastQuarterStartDate = getLastQuarterStartDate();
        cal.setTime(lastQuarterStartDate);
        cal.add(Calendar.MONTH, 3);
        cal.add(Calendar.DATE, -1);

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();

    }

    static boolean betweenLastQuarter(String date) {

        java.util.Date givenDate = null;

        try {
            givenDate = indexerDateFormat.parse(date);
        } catch (Exception e) {
            // TODO: handle exception
        }

        if (givenDate != null && givenDate.after(getLastQuarterStartDate())
                && givenDate.before(getLastQuarterEndDate())) {

            return true;
        }

        return false;

    }

    static Date getTodaysStartHour() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();

    }

    static Date getTodaysEndHour() {

        Calendar cal = Calendar.getInstance();

        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);

        return cal.getTime();

    }

    static String getTimeDetails(Date givenDate) {

        final DateFormat dateformat1 = new SimpleDateFormat("hh:mm a");
        dateformat1.setTimeZone(TimeZone.default);

        final DateFormat dateformat2 = new SimpleDateFormat("dd-MM-yyyy");
        dateformat2.setTimeZone(TimeZone.default);

        String modifiedAgo = "";

        if (equalsToday(givenDate)) {

            return "Today".concat(" at ").concat(dateformat1.format(givenDate));

        } else if (equalsYesterday(givenDate)) {

            return "Yesterday".concat(" at ").concat(dateformat1.format(givenDate));

        } else {

            Date currentDate = new Date();

            // Get milliseconds from each and subtract.
            long diffSeconds = (currentDate.getTime() - givenDate.getTime()) / 1000;

            long diffInDays = diffSeconds / (24 * 60 * 60);
            long remainingSecs = diffSeconds % (24 * 60 * 60);

            long diffHours = 0;
            if (remainingSecs > 0) {

                diffHours = remainingSecs / (60 * 60);
                remainingSecs = remainingSecs % (60 * 60);

            }

            long diffMinutes = 0;
            if (remainingSecs > 0) {
                diffMinutes = remainingSecs / (60);
                remainingSecs = remainingSecs % (60);
            }

            if (diffInDays > 0) {
                modifiedAgo = modifiedAgo.concat(diffInDays + " days ");
            }

            if (diffHours > 0) {
                modifiedAgo = modifiedAgo.concat(diffHours + " hours ");
            }

            if (diffMinutes > 0) {
                modifiedAgo = modifiedAgo.concat(diffMinutes + " minutes");
            }

            modifiedAgo = modifiedAgo.concat(" ago");
        }

        return modifiedAgo;

    }

    static String getFormattedDateTime(Date givenDate) {

        if (givenDate == null) return "";
        final DateFormat dateformat1 = new SimpleDateFormat("hh:mm a");
        dateformat1.setTimeZone(TimeZone.default);

        final DateFormat dateformat2 = new SimpleDateFormat("dd-MM-yyyy");
        dateformat2.setTimeZone(TimeZone.default);

        String dateTime = "";

        if (equalsToday(givenDate)) {

            dateTime = "".concat(" at ").concat(dateformat1.format(givenDate));

        } else if (equalsYesterday(givenDate)) {

            dateTime = "Yesterday".concat(" at ").concat(dateformat1.format(givenDate));

        } else {

            dateTime = dateformat2.format(givenDate).concat(" at ").concat(dateformat1.format(givenDate));

        }

        return dateTime;

    }

    static String getMessageFormattedDateTime(long givenDateMS, String timeZone) {

        Date date = new Date(givenDateMS);

        return getMessageFormattedDateTime(date, timeZone);
    }

    static String getMessageFormattedDateTime(Date givenDate, String timeZone) {

        if (givenDate == null)
            return "";

        final DateFormat dateformat1 = new SimpleDateFormat("HH:mm");
        dateformat1.setTimeZone(TimeZone.getTimeZone(timeZone));

        final DateFormat dateformat2 = new SimpleDateFormat("dd-MM-yyyy");
        dateformat2.setTimeZone(TimeZone.getTimeZone(timeZone));

        final DateFormat dateformat3 = new SimpleDateFormat("EEE");
        dateformat3.setTimeZone(TimeZone.getTimeZone(timeZone));

        String dateTime = "";

        if (equalsToday(givenDate)) {

            dateTime = dateformat1.format(givenDate);

        } else if (containsThisWeekByDay(givenDate)) {

            dateTime = dateformat3.format(givenDate);

        } else {

            dateTime = dateformat2.format(givenDate);

        }

        return dateTime;
    }


    static String getMessageFormattedDateTime(Date givenDate) {


        return getMessageFormattedDateTime(givenDate, TimeZone.default);

    }

    static String formatDate(Date givenDate) {
        final DateFormat dateformat2 = new SimpleDateFormat("dd MMMM yyyy");
        dateformat2.setTimeZone(TimeZone.default);

        String dateTime = "";
        dateTime = dateformat2.format(givenDate);

        return dateTime;

    }

    static DateFormat getSimpleDateFormat(String dataFormat) {
        final DateFormat dateformat = new SimpleDateFormat(dataFormat);
        dateformat.setTimeZone(TimeZone.default);

        return dateformat;

    }


    static Date parseDate(String givenDate, String format) {
        DateFormat dateformat = new SimpleDateFormat(format);

        if (givenDate == null) return null;

        try {
            return dateformat.parse(givenDate);
        } catch (ParseException e) {

            return null;
        }

    }



    static Date parseDate(String givenDate, List<String> dateFormats) {

        Date date = null;

        if (givenDate == null) return null;

        for (String format : dateFormats) {

            DateFormat dateformat = new SimpleDateFormat(format);

            try {
                dateformat.setLenient(false);
                date = dateformat.parse(givenDate);
            } catch (ParseException e) {
            }
            if (date != null) {
                break;
            }
        }

        return date;

    }


    static String getDay(String givenDate, String format, int day) {
        DateFormat dateformat = new SimpleDateFormat(format);
        Date date = null;
        if (givenDate){
            try {
                date = dateformat.parse(givenDate);
            } catch (ParseException e) {
                return null;
            }
        } else {
            date = new Date();
        }

        Calendar cl = Calendar.getInstance();
        cl.setTime(date);
        cl.add(Calendar.DATE, day);
        return dateformat.format(cl.getTime());


    }

    static long lowestTimeOfDayInMillis(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendar.HOUR_OF_DAY, 0);
        calendar.set(calendar.MINUTE, 0);
        calendar.set(calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    static boolean isGivenDateinDateRange(Date givenDate, Date formDate, Date toDate){

        if( (lowestTimeOfDayInMillis(givenDate)>=lowestTimeOfDayInMillis(formDate)) &&
                (lowestTimeOfDayInMillis(givenDate)<=lowestTimeOfDayInMillis(toDate)) ){
            return true;
        }

        return false;
    }

}
