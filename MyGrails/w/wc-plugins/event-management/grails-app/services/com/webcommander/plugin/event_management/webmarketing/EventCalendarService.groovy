package com.webcommander.plugin.event_management.webmarketing

import com.webcommander.plugin.event_management.Event
import com.webcommander.util.AppUtil
import org.grails.plugins.web.taglib.ApplicationTagLib
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import java.text.SimpleDateFormat

class EventCalendarService  {
    @Autowired
    @Qualifier("org.grails.plugins.web.taglib.ApplicationTagLib")
    ApplicationTagLib g

    def static veryShortDayNames =["s", "m", "t", "w", "t", "f", "s"]

    private getCalendarDayView(date){
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        int month = calendar.get(Calendar.MONTH)
        int year = calendar.get(Calendar.YEAR)
        int day = calendar.get(Calendar.DATE)
        calendar.set(year, month, day)
        Closure generateTdDom;
        StringWriter tempOut = new StringWriter()
        tempOut << "<div class='calendar calendar-day-view'><div class='header'><span class='navigation-panel'>" +
                "<span class='navigator previous mark-icon'></span><span class='navigator next mark-icon'></span></span>" +
                "<span class='date-month-year'><span class='date'>" + calendar.get(Calendar.DATE) + "</span><span class='month'>" + new SimpleDateFormat("MMMM").format(calendar.getTime()) + "</span><span class='year'>" + year + "</span></span>" +
                "</div><div class='body'><table><colgroup><col style='width: 10%'><col style='width: 90%'></colgroup>";
        tempOut << '<input type="hidden" name="selected-day" value="' + day + '">'
        tempOut << '<input type="hidden" name="selected-month" value="' + (month + 1) + '">'
        tempOut << '<input type="hidden" name="selected-year" value="' + year + '">'
        generateTdDom = { time, amPm  ->
            tempOut << "<tr><td><span class='time'>" + time + "</span><span class='minute'>" + amPm + "</span></td><td class='event-container'></td></tr>"
        }
        generateTdDom(12, "am")
        for(int i = 1; i <= 11 ; i++){
            generateTdDom(i, "am")
        }
        generateTdDom(12, "pm")
        for(int i = 1; i <= 11 ; i++){
            generateTdDom(i, "pm")
        }
        tempOut << "</table></div></div>"
        return tempOut.toString();
    }

    private getCalendarWeekView(date){
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        int month = calendar.get(Calendar.MONTH)
        int year = calendar.get(Calendar.YEAR)
        int day = calendar.get(Calendar.DATE)
        Calendar tempCalendar = Calendar.getInstance()
        tempCalendar.setTime(calendar.getTime())
        tempCalendar.add(Calendar.DATE, 6)
        int lastDayMonth = tempCalendar.get(Calendar.MONTH)
        Closure generateTdDom;
        StringWriter tempOut = new StringWriter()
        tempOut << "<div class='calendar calendar-week-view'><div class='header'><span class='navigation-panel'><span class='navigator previous mark-icon'></span><span class='navigator next " +
                "mark-icon'>N</span></span><span class='date-month-year'>";
        if(month == lastDayMonth){
            tempOut << "<span class='first-day'><span class='day'>" + day + "</span></span>-<span class='last-day'><span class='day'>" + tempCalendar.get(Calendar.DATE) +
                    "</span><span class='month'>" + new SimpleDateFormat("MMMM").format(calendar.getTime()) + "</span><span class='year'>" +  year + "</span></span>"
        } else {
            tempOut << "<span class='first-day'><span class='day'>" + day + "</span><span class='month'>" + new SimpleDateFormat("MMMM").format(calendar.getTime()) + "</span><span class='year'>" +
                    year + "</span></span>-<span class='last-day'><span class='day'>" + tempCalendar.get(Calendar.DATE) + "</span><span class='month'>" +
                    new SimpleDateFormat("MMMM").format(tempCalendar.getTime()) + "</span><span class='year'>" +  tempCalendar.get(Calendar.YEAR) + "</span></span>"
        }
        tempOut <<"</span></div><div class='body'><table><colgroup><col style='width: 9%'><col style='width: 13%'><col style='width: 13%'>" +
                "<col style='width: 13%'><col style='width: 13%'><col style='width: 13%'><col style='width: 13%'><col style='width: 13%'></colgroup>";
        tempOut << '<input type="hidden" name="selected-day" value="' + day + '">'
        tempOut << '<input type="hidden" name="selected-month" value="' + (month + 1) + '">'
        tempOut << '<input type="hidden" name="selected-year" value="' + year + '">'
        generateTdDom = {t, amPm  ->
            tempOut << "<tr><td><span class='time'>" + t + "</span><span class='minute'>" + amPm + "</span></td><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>"
        }
        generateTdDom(12, "am")
        for(int i = 1; i <= 11 ; i++){
            generateTdDom(i, "am")
        }
        generateTdDom(12, "pm")
        for(int i = 1; i <= 11 ; i++){
            generateTdDom(i, "pm")
        }
        tempOut << "</table></div></div>"
        return tempOut.toString();
    }

    private String getCalendarMonthView(date){
        def timezone = AppUtil.session.timezone
        Date today = new Date().gmt().toZone(timezone)
        Calendar calendar = Calendar.getInstance()
        def daysInMonth = { y, m ->
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(y, m, 1))
            return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        }
        calendar.setTime(date)
        int month = calendar.get(Calendar.MONTH)
        int year = calendar.get(Calendar.YEAR)
        calendar.set(year, month, 1)
        int day = calendar.get(Calendar.DAY_OF_WEEK)
        int currentMonthDayCount = daysInMonth(year, month)
        int previousMonthDayCount = daysInMonth(year, month - 1)
        int previousDayCount = day - 1;
        int rowCount = Math.ceil(((currentMonthDayCount + previousDayCount))/7)
        Closure generateTdDom;
        StringWriter tempOut = new StringWriter()
        tempOut << "<div class='calendar calendar-month-view'><div class='header'><span class='navigation-panel'>" +
                "<span class='navigator previous mark-icon'></span><span class='navigator next mark-icon'></span></span>" +
                "<span class='date-month-year'>" + "<span class='month'>" + new SimpleDateFormat("MMMM").format(calendar.getTime()) + "</span><span class='year'>" + year + "</span></span>" +
                "</div><div class='body'><table><thead><tr>";
        tempOut << '<input type="hidden" name="selected-month" value="' + (month + 1) + '">'
        tempOut << '<input type="hidden" name="selected-year" value="' + year + '">'
        def dayNames = veryShortDayNames
        dayNames.each {
            tempOut << "<th>" + g.message(code: it) + "</th>"
        }
        tempOut << "</tr></thead><tr class='first-row' week='1'>"
        int dayNo = previousMonthDayCount - previousDayCount + 1;
        int week = 2;
        Date todayEndTime = today.dayEnd
        generateTdDom = { c, otherMonth ->
            Date d = c.getTime()
            Boolean weekend = false
            Boolean isToday = todayEndTime == d.dayEnd
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
            if(dayOfWeek == 7 || dayOfWeek == 1){
                weekend = true
            }
            if(otherMonth){
                tempOut << "<td class='other-month'><span class='date'>" + dayNo ++ + "</span>"
            } else {
                String classes = "current" + (weekend ? " weekend-day" : "") + (isToday ? " today" : "")
                tempOut << "<td class='" + classes + "' date='" + dayNo + "'><span class='date'>" + dayNo ++ + "</span>"
            }
            tempOut << "</td>"
            c.add(Calendar.DATE, 1)
        }
        Calendar tempCalendar = Calendar.getInstance()
        tempCalendar.set(year, month - 1, dayNo)
        for(int i = 0; i < previousDayCount; i++) {
            generateTdDom(tempCalendar, true)
        }
        dayNo = 1
        for(int i = 0; i < (8 - day); i++) {
            generateTdDom(calendar, false)
        }
        tempOut << "</tr>"
        for(int i = 0; i < (rowCount - 2); i++){
            tempOut << "<tr week='" + week++ + "'>"
            for(int j = 0; j < 7; j++){
                generateTdDom(calendar, false)
            }
            tempOut << "</tr>"
        }
        tempOut << "<tr class='last-row' week='" + week + "'>"
        int remainingDay = currentMonthDayCount - dayNo + 1
        for (int i = 0; i < remainingDay; i++) {
            generateTdDom(calendar, false)
        }
        dayNo = 1;
        tempCalendar.set(year, month + 1, 1)
        for (int i = 0; i < (7 - remainingDay); i++) {
            generateTdDom(tempCalendar, true)
        }
        tempOut << "</tr></table></div></div>"
        return tempOut.toString();
    }

    public Date getDateForCalendar(String view, String date, String operation){
        def timezone = AppUtil.session.timezone
        Date initDate = date ? date.dayStart : new Date().gmt().toZone(timezone)
        if(operation){
            Calendar calendar = Calendar.getInstance()
            calendar.setTime(initDate)
            if(view == "day"){
                if(operation == "a"){
                    calendar.add(Calendar.DATE, 1)
                } else {
                    calendar.add(Calendar.DATE, -1)
                }
            }
            else if(view == "week"){
                if(operation == "a"){
                    calendar.add(Calendar.DATE, 7)
                } else {
                    calendar.add(Calendar.DATE, -7)
                }
            }
            else {
                if(operation == "a"){
                    calendar.add(Calendar.MONTH, 1)
                } else {
                    calendar.add(Calendar.MONTH, -1)
                }
            }
            initDate = calendar.getTime()
        }
        return initDate
    }

    public def getEvents(Date date, String view){
        def timezone = AppUtil.session.timezone
        List<Event> events = Event.createCriteria().list {
            and "getClosureFor${view.capitalize()}ViewEvents"(date)
        }
        def eventList = []
        events.each {
            def event = [:]
            event.id = it.id
            event.start = it.startTime.toAdminFormat(true, false, timezone)
            event.end = it.endTime.toAdminFormat(true, false, timezone)
            event.text = it.name
            eventList.add(event)
        }
        return eventList
    }

    private Closure getClosureForDayViewEvents(Date date){
        def timezone = AppUtil.session.timezone
        return {
            ge("startTime", date.dayStart.gmt(timezone))
            le("endTime", date.dayEnd.gmt(timezone))
        }
    }

    private Closure getClosureForWeekViewEvents(Date date){
        def timezone = AppUtil.session.timezone
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        Date start = calendar.getTime()
        calendar.add(Calendar.DATE, 6)
        Date end = calendar.getTime()
        return {
            ge("startTime",start.dayStart.gmt(timezone))
            le("startTime", end.dayStart.gmt(timezone))
        }
    }

    private Closure getClosureForMonthViewEvents(Date date){
        def timezone = AppUtil.session.timezone
        Calendar calendar = Calendar.getInstance()
        calendar.setTime(date)
        calendar.set(Calendar.DATE, 1)
        Date start = calendar.getTime()
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE))
        Date end = calendar.getTime()
        return {
            ge("startTime",start.dayStart.gmt(timezone))
            le("startTime", end.dayStart.gmt(timezone))
        }
    }
}
