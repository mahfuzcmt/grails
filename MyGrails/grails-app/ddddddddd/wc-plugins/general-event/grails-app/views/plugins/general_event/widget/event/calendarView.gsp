<%@ page import="com.webcommander.util.AppUtil" %>
<g:applyLayout name="_widget">
    <g:set var="id" value="${UUID.randomUUID().toString()}"/>
    <div class="calendar-wrap ${config.displayType}">
        <g:if test="${config.displayType == 'basic-calendar' && config.detailsOn == 'click'}">
            <div class="event-summary sidebar"></div>
        </g:if>
        <div id='calendar-${id}' class="calendar ${config.displayType == 'basic-calendar' ? 'fc-basic-calendar' : ''}">Loading...</div>
        <input type="hidden" class="dateFormat" value="${AppUtil.getConfig('locale', 'admin_date_format')}">
        <input type="hidden" class="timeFormat" value="${AppUtil.getConfig('locale', 'admin_time_format')}">
    </div>
    <g:if test="${request.page}">
        <%
            if(!request.is_fullcalendar_loaded) {
                request.js_cache.push("plugins/general-event/fullcalendar/fullcalendar.min.js")
                request.css_cache.push("plugins/general-event/fullcalendar/fullcalendar.css")
                request.is_fullcalendar_loaded = true;
            }

            if(!request.is_event_widget_calendar_script_loaded) {
                request.js_cache.push("plugins/general-event/js/widget/calendar-widget.js")
                request.js_cache.push("plugins/general-event/js/widget/event-widget.js")
                request.is_event_widget_calendar_script_loaded = true;
            }
        %>
        <script type="text/javascript">
            $(function() {
                bm.onReady(app, "initCalendarWidgetForGeneralEvent", function() {
                    var config = {};
                    config.dateFormat = $('.dateFormat').val().replaceAll('E', 'd');
                    config.timeFormat = $('.timeFormat').val().replace('aaa', 'TT');
                    config.dateTimeFormat = config.dateFormat + ' ' + config.timeFormat;
                    config.isBasicCalendar = ${config.displayType.indexOf('basic-') == 0};
                    config.showPrice = ${config.showPrice.toInteger()};
                    config.showRequestInfo = ${config.showRequestInfo.toInteger()};
                    config.showBookNow = ${config.showBookNow.toInteger()};
                    config.labelForBookNow = '${config.labelForBookNow}';
                    config.detailsOn = '${config.detailsOn}';
                    config.dayChar = ${config.dayChar.toInteger()};
                    var eventDataList = ${eventDataList};
                    app.initCalendarWidgetForGeneralEvent($('#calendar-' + '${id}'), config, eventDataList);
                })
            })
        </script>
    </g:if>
    <g:else>
        <input type="hidden" class="eventDataList" value='${eventDataList.encodeAsBMHTML()}'>
    </g:else>
</g:applyLayout>